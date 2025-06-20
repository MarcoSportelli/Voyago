package com.example.voyago.components
import LocationHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import android.Manifest
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume

@Composable
fun DraggableLocationMap(
    modifier: Modifier = Modifier,
    initialLocation: LatLng = LatLng(0.0, 0.0),
    address: String? = null,
    onLocationChanged: (LatLng) -> Unit = {},
    isEditMode: Boolean = false
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.all { it.value }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val locationHelper = remember { LocationHelper(context) }
    var markerLocation by remember { mutableStateOf<LatLng?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Funzione per aprire Google Maps
    fun openGoogleMaps(latLng: LatLng) {
        val uri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback per dispositivi senza Google Maps
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${latLng.latitude},${latLng.longitude}")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        }
    }

    // Funzione di geocoding
    suspend fun geocodeAddress(addressString: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context)

            suspendCancellableCoroutine { continuation ->
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(addressString, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val location = addresses[0]
                        continuation.resume(LatLng(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: IOException) {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // Elabora l'indirizzo se fornito
    LaunchedEffect(address) {
        if (!address.isNullOrBlank()) {
            isLoading = true
            val geocodedLocation = geocodeAddress(address)
            if (geocodedLocation != null) {
                markerLocation = geocodedLocation
                onLocationChanged(geocodedLocation)
            } else {
                errorMessage = "Impossibile trovare la posizione per l'indirizzo: $address"
                if (initialLocation != LatLng(0.0, 0.0)) {
                    markerLocation = initialLocation
                }
            }
            isLoading = false
        }
    }

    // Se non viene fornito un indirizzo o il geocoding fallisce, usa la posizione utente o quella iniziale
    LaunchedEffect(hasLocationPermission, address) {
        if (markerLocation == null) {
            if (hasLocationPermission) {
                locationHelper.getCurrentLocation(
                    onSuccess = { location ->
                        markerLocation = location
                        isLoading = false
                    },
                    onFailure = { e ->
                        errorMessage = e.message
                        if (initialLocation != LatLng(0.0, 0.0)) {
                            markerLocation = initialLocation
                        }
                        isLoading = false
                    }
                )
            } else {
                if (initialLocation != LatLng(0.0, 0.0)) {
                    markerLocation = initialLocation
                }
                isLoading = false
            }
        }
    }

    val markerState = rememberMarkerState(
        position = markerLocation ?: initialLocation
    )

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(markerLocation) {
        markerLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
            markerState.position = it
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (markerLocation != null) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = hasLocationPermission,
                    zoomControlsEnabled = true
                ),
                onMapClick = { clickedLatLng ->
                    if (isEditMode){
                        markerState.position = clickedLatLng
                        onLocationChanged(clickedLatLng)
                    }else{
                        openGoogleMaps(markerLocation!!)
                    }

                },
            ) {
                Marker(
                    state = markerState,
                    title = address ?: "Destinazione",
                    draggable = true,
                    onInfoWindowClick = {
                        openGoogleMaps(markerState.position)
                    }
                )
            }
        }

        errorMessage?.let {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}