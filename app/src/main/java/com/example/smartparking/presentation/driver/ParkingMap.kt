package com.example.smartparking.presentation.driver


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.tasks.await
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val DhakaCenter = LatLng(23.7808, 90.4142)

private data class MapParkingLocation(
    val name: String,
    val position: LatLng,
    val availableSlots: Int
)

private val mapParkingLocations = listOf(
    MapParkingLocation(
        "Dhanmondi City Parking",
        LatLng(23.7465, 90.3760),
        12
    ),
    MapParkingLocation(
        "Bashundhara Parking Hub",
        LatLng(23.8223, 90.4250),
        8
    ),
    MapParkingLocation(
        "Mirpur Smart Parking",
        LatLng(23.8067, 90.3686),
        20
    ),
    MapParkingLocation(
        "Gulshan Central Parking",
        LatLng(23.7925, 90.4078),
        5
    )
)

@Composable
fun ParkingMap() {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DhakaCenter, 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val location = fusedLocationClient.lastLocation.await()

                if (location != null) {
                    val userLocation = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            userLocation,
                            14f
                        )
                    )
                }
            } catch (_: SecurityException) {
                // Location permission was not available.
            } catch (_: Exception) {
                // Last known location is unavailable.
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = hasLocationPermission,
                compassEnabled = true
            )
        ) {
            mapParkingLocations.forEach { parking ->
                Marker(
                    state = remember(parking.name) {
                        MarkerState(position = parking.position)
                    },
                    title = parking.name,
                    snippet = "${parking.availableSlots} sample slots available"
                )
            }
        }

        if (!hasLocationPermission) {
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3985FF)
                )
            ) {
                Text("Use My Location")
            }
        }
    }
}
