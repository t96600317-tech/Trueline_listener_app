package com.example.trueline_listener.audio

import androidx.compose.runtime.Composable

@Composable
expect fun AudioPermissionRequester(
    content: @Composable (requestPermission: () -> Unit, hasPermission: Boolean) -> Unit
)
