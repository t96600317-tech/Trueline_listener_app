package com.example.trueline_listener.location

import androidx.compose.runtime.Composable

private object IOSLocationProvider : LocationProvider {
    override fun fetchLiveCity(onSuccess: (DetectedCityLocation) -> Unit, onError: (String) -> Unit) {
        onError("Location verification is not configured for iOS yet.")
    }

    override fun isLocationServiceEnabled(): Boolean = true
}

actual fun getLocationProvider(): LocationProvider = IOSLocationProvider

actual fun openLocationSettings() = Unit

@Composable
actual fun rememberLocationPermissionRequester(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): () -> Unit = { onPermissionDenied() }
