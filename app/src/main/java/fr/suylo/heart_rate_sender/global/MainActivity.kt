package fr.suylo.heart_rate_sender.global

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import fr.suylo.heart_rate_sender.global.theme.HeartRateSenderTheme

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            HeartRateSenderTheme {
                WearApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun WearApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        TimeText()
        HeartRateControls()
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun HeartRateControls() {
    val context = LocalContext.current
    var isMonitoring by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            startHeartRateService(context)
            isMonitoring = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF262626),
                        Color(0xFF260101)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeartbeatIcon()

        Text(
            text = "FC • Sender",
            color = Color.White,
            style = MaterialTheme.typography.title2,
            modifier = Modifier.padding(6.dp)
        )

        Text(
            text = if (isMonitoring) "Service : en cours" else "Service : arrêté",
            color = Color(0xFFB9B9B9),
            style = MaterialTheme.typography.caption2,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (hasHeartRatePermissions(context)) {
                        startHeartRateService(context)
                        isMonitoring = true
                    } else {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.BODY_SENSORS,
                                Manifest.permission.FOREGROUND_SERVICE_HEALTH
                            )
                        )
                    }
                },
                enabled = !isMonitoring,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isMonitoring) Color.Gray else Color(0xFF81C784)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Démarrer",
                    tint = Color.White
                )
            }

            Button(
                onClick = {
                    stopHeartRateService(context)
                    isMonitoring = false
                },
                enabled = isMonitoring,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (!isMonitoring) Color.Gray else Color(0xFFE57373)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Arrêter",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun HeartbeatIcon() {
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
            ) { value, _ ->
                scale = value
            }
            animate(
                initialValue = 1.2f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
            ) { value, _ ->
                scale = value
            }
        }
    }

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "«",
            color = Color(0xFFB4B4B4),
            modifier = Modifier.padding(4.dp)
        )

        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = "Battement de coeur",
            tint = Color(0xFFFF5858),
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )

        Text(
            text = "»",
            color = Color(0xFFB4B4B4),
            modifier = Modifier.padding(4.dp)
        )
    }


}


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private fun hasHeartRatePermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BODY_SENSORS
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE_HEALTH
            ) == PackageManager.PERMISSION_GRANTED
}

private fun startHeartRateService(context: Context) {
    val intent = Intent(context, HeartRateService::class.java)
    context.startService(intent)
}

private fun stopHeartRateService(context: Context) {
    val intent = Intent(context, HeartRateService::class.java)
    context.stopService(intent)
}
