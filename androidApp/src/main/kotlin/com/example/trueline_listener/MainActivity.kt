package com.example.trueline_listener

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.trueline_listener.onboarding.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Android Audio Engine for real microphone recording & playback
        com.example.trueline_listener.audio.initAudioEngine(this)

        // Initialize Android Session Storage for persistent JWT and state restoration
        com.example.trueline_listener.storage.initSessionStorage(this)

        // Ensure status bar icons are dark (for light theme)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            val scope = rememberCoroutineScope()
            val onboardingViewModel = remember { OnboardingViewModel(scope) }

            BackHandler(enabled = true) {
                // If user is inside onboarding steps, go back to previous step
                if (!onboardingViewModel.goBack()) {
                    // If on root screen (Phone Input), require double press within 2s to exit
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