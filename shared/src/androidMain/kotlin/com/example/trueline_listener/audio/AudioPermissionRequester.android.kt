package com.example.trueline_listener.audio

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun AudioPermissionRequester(
    content: @Composable (requestPermission: () -> Unit, hasPermission: Boolean) -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val isGranted = permissionState.status.isGranted

    content(
        { permissionState.launchPermissionRequest() },
        isGranted
    )
}
