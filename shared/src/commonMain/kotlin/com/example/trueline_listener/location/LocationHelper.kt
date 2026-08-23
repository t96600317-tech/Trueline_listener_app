package com.example.trueline_listener.location

import androidx.compose.runtime.Composable

data class DetectedCityLocation(
    val city: String,
    val state: String,
    val formatted: String,
    val isGpsVerified: Boolean = true
)

interface LocationProvider {
    fun fetchLiveCity(onSuccess: (DetectedCityLocation) -> Unit, onError: (String) -> Unit)
    fun isLocationServiceEnabled(): Boolean
}

expect fun getLocationProvider(): LocationProvider

expect fun openLocationSettings()

@Composable
expect fun rememberLocationPermissionRequester(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): () -> Unit
