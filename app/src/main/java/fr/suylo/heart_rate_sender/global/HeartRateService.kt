package fr.suylo.heart_rate_sender.global

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HeartRateService : Service(), SensorEventListener {

    private val tag = "HeartRateService"
    private val channelId = "HeartRateChannel"
    private val notificationId = 1

    private lateinit var sensorManager: SensorManager
    private var hrSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(notificationId, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        hrSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }

        vibrate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        sendBroadcast(Intent("fr.suylo.heart_rate_sender.ACTION_SERVICE_STOPPED"))
        vibrate()
        Log.d(tag, "Mesure stoppée.")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values[0].toInt()
            Log.d(tag, "BPM détecté : $bpm")
            sendBpmToApi(bpm)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non utilisé
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Surveillance FC",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Service de surveillance de la fréquence cardiaque"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingMain = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("FC • Sender")
            .setContentText("Envoie en cours…")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingMain)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                50,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    private fun sendBpmToApi(bpm: Int) {
        val client = OkHttpClient()

        val json = """{"bpm": $bpm, "timestamp": ${System.currentTimeMillis()}}"""
        val body = json.toRequestBody("application/json".toMediaType())

        // Serveur privée accessible uniquement via VPN
        val request = Request.Builder()
            .url("http://100.100.3.22:3000/bpm")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "Erreur envoi BPM : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(tag, "BPM $bpm envoyé avec succès")
                } else {
                    Log.e(tag, "Erreur serveur: ${response.code}")
                }
            }
        })
    }
}
