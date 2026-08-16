package com.example.trueline_listener

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.trueline_listener.onboarding.*
import com.example.trueline_listener.ui.theme.TrueLineTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { OnboardingViewModel(scope) }
    val currentStep by viewModel.currentStep.collectAsState()

    TrueLineTheme(useDarkTheme = false) { // Forcing Light Theme for onboarding as requested
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .fillMaxSize()
            ) {
                when (currentStep) {
                    OnboardingStep.PHONE_INPUT -> {
                        PhoneInputScreen(viewModel)
                    }
                    OnboardingStep.OTP_VERIFICATION -> {
                        OtpVerificationScreen(viewModel)
                    }
                    OnboardingStep.PROFILE_SETUP -> {
                        BasicProfileScreen(viewModel)
                    }
                    OnboardingStep.VOICE_INTRO -> {
                        VoiceIntroScreen(viewModel)
                    }
                    OnboardingStep.FACE_VERIFICATION -> {
                        FaceVerificationScreen(viewModel)
                    }
                    OnboardingStep.KYC_DOCUMENT -> {
                        // Placeholder for Step 5
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Step 5: KYC Document (Coming Soon)",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
