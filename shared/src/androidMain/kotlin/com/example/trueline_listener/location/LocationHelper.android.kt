package com.example.trueline_listener.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.trueline_listener.ui.IndianCitiesData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private var androidContext: Context? = null

class AndroidLocationProvider(private val context: Context) : LocationProvider {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return true
        return try {
            val isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            isGps || isNetwork
        } catch (e: Exception) {
            true
        }
    }

    override fun fetchLiveCity(onSuccess: (DetectedCityLocation) -> Unit, onError: (String) -> Unit) {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!finePerm && !coarsePerm) {
            onError("Location permission is required. Please allow location access.")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("Location service is unavailable on this device.")
            return
        }

        // 1. Immediately check best last known location from all available providers
        val allProviders = listOf("fused", LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        var bestLastKnown: Location? = null
        for (provider in allProviders) {
            try {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    if (bestLastKnown == null || loc.accuracy < bestLastKnown.accuracy) {
                        bestLastKnown = loc
                    }
                }
            } catch (e: Exception) {
                Log.w("LocationProvider", "Exception reading $provider: ${e.message}")
            }
        }

        if (bestLastKnown != null) {
            geocodeAndReturn(bestLastKnown.latitude, bestLastKnown.longitude, onSuccess) {
                listenForLiveLocation(locationManager, bestLastKnown, onSuccess, onError)
            }
            return
        }

        // 2. No last known location, listen for live GPS / Network updates
        listenForLiveLocation(locationManager, null, onSuccess, onError)
    }

    private fun listenForLiveLocation(
        locationManager: LocationManager,
        fallbackLoc: Location?,
        onSuccess: (DetectedCityLocation) -> Unit,
        onError: (String) -> Unit
    ) {
        var updateDispatched = false
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (!updateDispatched) {
                    updateDispatched = true
                    try { locationManager.removeUpdates(this) } catch (e: Exception) {}
                    geocodeAndReturn(loc.latitude, loc.longitude, onSuccess) {
                        onError("Could not determine city name from GPS coordinates.")
                    }
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val activeProviders = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        for (p in activeProviders) {
            try {
                if (locationManager.isProviderEnabled(p)) {
                    locationManager.requestLocationUpdates(p, 0L, 0f, listener, Looper.getMainLooper())
                }
            } catch (e: Exception) {
                Log.w("LocationProvider", "Failed to register $p: ${e.message}")
            }
        }

        scope.launch {
            delay(5000)
            if (!updateDispatched) {
                updateDispatched = true
                try { locationManager.removeUpdates(listener) } catch (e: Exception) {}
                if (fallbackLoc != null) {
                    geocodeAndReturn(fallbackLoc.latitude, fallbackLoc.longitude, onSuccess) {
                        onError("Unable to resolve city name. Please select below.")
                    }
                } else {
                    onError("Could not get GPS fix. Please ensure location is enabled.")
                }
            }
        }
    }

    private fun geocodeAndReturn(
        latitude: Double,
        longitude: Double,
        onSuccess: (DetectedCityLocation) -> Unit,
        onFailure: () -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val resolved = reverseGeocode(latitude, longitude)
            if (resolved != null) {
                val locality = resolved.first
                val state = resolved.second

                val matchedCity = matchWithIndianCities(locality, state)
                withContext(Dispatchers.Main) {
                    val resultCity = matchedCity.split(",")[0].trim()
                    val resultState = if (matchedCity.contains(",")) matchedCity.split(",")[1].trim() else state
                    onSuccess(
                        DetectedCityLocation(
                            city = resultCity,
                            state = resultState,
                            formatted = "$resultCity, $resultState",
                            isGpsVerified = true
                        )
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    onFailure()
                }
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String>? {
        // 1. Android Native Geocoder
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.Builder().setLanguage("en").setRegion("IN").build())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val state = addr.adminArea
                if (!locality.isNullOrBlank()) {
                    return Pair(locality, state ?: "India")
                }
            }
        } catch (e: Exception) {
            Log.w("LocationProvider", "Android Geocoder error: ${e.message}")
        }

        // 2. High-speed HTTP Reverse Geocoder using actual GPS coordinates (NO IP lookup)
        try {
            val url = URL("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=en")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val city = json.optString("city").takeIf { it.isNotBlank() }
                    ?: json.optString("locality").takeIf { it.isNotBlank() }
                    ?: json.optString("principalSubdivision").takeIf { it.isNotBlank() }
                val state = json.optString("principalSubdivision").takeIf { it.isNotBlank() } ?: "India"
                if (!city.isNullOrBlank()) {
                    return Pair(city, state)
                }
            }
        } catch (e: Exception) {
            Log.w("LocationProvider", "BigDataCloud reverse geocode error: ${e.message}")
        }

        return null
    }

    private fun matchWithIndianCities(locality: String, state: String): String {
        val directMatch = IndianCitiesData.cities.find {
            val cName = it.split(",")[0].trim()
            cName.equals(locality, ignoreCase = true) || 
            locality.contains(cName, ignoreCase = true) ||
            cName.contains(locality, ignoreCase = true)
        }
        if (directMatch != null) return directMatch

        val stateMatch = IndianCitiesData.cities.find {
            it.contains(state, ignoreCase = true)
        }
        if (stateMatch != null) return "$locality, $state"

        return "$locality, $state"
    }
}

private var globalLocationProvider: LocationProvider? = null

fun initLocationProvider(context: Context) {
    androidContext = context.applicationContext
    globalLocationProvider = AndroidLocationProvider(context.applicationContext)
}

actual fun getLocationProvider(): LocationProvider {
    return globalLocationProvider ?: object : LocationProvider {
        override fun isLocationServiceEnabled(): Boolean = true
        override fun fetchLiveCity(onSuccess: (DetectedCityLocation) -> Unit, onError: (String) -> Unit) {
            onError("Location provider is not initialized.")
        }
    }
}

actual fun openLocationSettings() {
    try {
        val ctx = androidContext
        if (ctx != null) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        }
    } catch (e: Exception) {
        Log.e("LocationHelper", "Failed to open location settings: ${e.message}")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberLocationPermissionRequester(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): () -> Unit {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val anyGranted = permissionState.permissions.any { it.status.isGranted }
    var userRequested by remember { mutableStateOf(false) }

    LaunchedEffect(anyGranted, userRequested) {
        if (userRequested && anyGranted) {
            userRequested = false
            onPermissionGranted()
        }
    }

    return {
        if (anyGranted) {
            onPermissionGranted()
        } else {
            userRequested = true
            permissionState.launchMultiplePermissionRequest()
        }
    }
}
