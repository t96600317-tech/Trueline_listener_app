package com.example.trueline_listener

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import com.example.trueline_listener.onboarding.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Audio Engine & Session Storage & Location Provider & Call Service
        com.example.trueline_listener.audio.initAudioEngine(this)
        com.example.trueline_listener.storage.initSessionStorage(this)
        com.example.trueline_listener.location.initLocationProvider(this)
        com.example.trueline_listener.call.initCallService(this)

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
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}