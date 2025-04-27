package fr.suylo.heart_rate_sender.global

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.health.services.client.awaitWithException
import androidx.health.services.client.unregisterMeasureCallback

class HeartRateService : Service() {

    private val tag = "HeartRateService"
    private val channelId = "HeartRateChannel"
    private lateinit var measureClient: MeasureClient
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val measureCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            if (availability is DataTypeAvailability) {
                Log.d(tag, "Disponibilité : $availability")
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            val heartRateData = data.getData(DataType.HEART_RATE_BPM)
            if (heartRateData.isNotEmpty()) {
                val heartRate = heartRateData.last().value
                Log.d(tag, "Fréquence Cardiaque : $heartRate")
                // TODO: Envoyer les données au serveur
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)

        measureClient = HealthServices.getClient(this).measureClient

        serviceScope.launch {
            val capabilities = measureClient.getCapabilitiesAsync().awaitWithException()
            if (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure) {
                measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, measureCallback)
                Log.d(tag, "Mesure démarrée.")
            } else {
                Log.e(tag, "Heart rate measurement not supported.")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, measureCallback)
            Log.d(tag, "Mesure stoppée.")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("H.R. Watcher")
            .setContentText("En cours...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }


}