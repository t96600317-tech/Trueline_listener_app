package com.example.trueline_listener

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.trueline_listener.onboarding.OnboardingViewModel

class MainActivity : AppCompatActivity() {
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Audio Engine & Session Storage & Location Provider & Call Service
        com.example.trueline_listener.audio.initAudioEngine(this)
        com.example.trueline_listener.storage.initSessionStorage(this)
        com.example.trueline_listener.otp.initMsg91Otp(
            widgetId = BuildConfig.MSG91_WIDGET_ID,
            authToken = BuildConfig.MSG91_AUTH_TOKEN
        )
        com.example.trueline_listener.location.initLocationProvider(this)
        com.example.trueline_listener.call.initCallService(this)
        com.example.trueline_listener.call.initIncomingCallAlert(this)
        com.example.trueline_listener.call.handleIncomingCallNotificationIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 201)
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            val scope = rememberCoroutineScope()
            val onboardingViewModel = remember { OnboardingViewModel(scope) }

            BackHandler(enabled = true) {
                if (!onboardingViewModel.goBack()) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            App(onboardingViewModel = onboardingViewModel)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        com.example.trueline_listener.call.handleIncomingCallNotificationIntent(intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
