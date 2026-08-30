package com.example.trueline_listener.audio

import androidx.compose.runtime.Composable

@Composable
actual fun AudioPermissionRequester(
    content: @Composable (requestPermission: () -> Unit, hasPermission: Boolean) -> Unit
) {
    content(requestPermission = {}, hasPermission = false)
}
