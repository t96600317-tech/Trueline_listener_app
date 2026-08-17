package com.example.trueline_listener.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun FaceVerificationCameraWrapper(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel,
    onPhotoCaptured: (String) -> Unit
)
