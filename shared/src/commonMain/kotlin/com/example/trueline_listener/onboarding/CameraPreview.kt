package com.example.trueline_listener.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(
    modifier: Modifier,
    onPhotoCaptured: (String) -> Unit
)
