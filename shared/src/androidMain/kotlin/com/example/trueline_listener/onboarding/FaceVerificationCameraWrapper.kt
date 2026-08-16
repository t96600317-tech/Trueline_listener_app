package com.example.trueline_listener.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun FaceVerificationCameraWrapper(
    modifier: Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
) {
    FaceVerificationCamera(modifier, viewModel, onPhotoCaptured)
}
