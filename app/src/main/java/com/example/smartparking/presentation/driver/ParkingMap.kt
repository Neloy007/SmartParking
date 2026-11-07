
package com.example.smartparking.presentation.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartparking.data.model.Parking
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val MapDhakaCenter = LatLng(23.7808, 90.4142)

private val MapBackground = Color(0xFF0B1220)
private val MapSurface = Color(0xFF151F30)
private val MapBlue = Color(0xFF3985FF)
private val MapText = Color(0xFFF1F5F9)
private val MapSecondary = Color(0xFF94A3B8)

@Composable
fun ParkingMap(
    parkings: List<Parking>,
    currentLocation: LatLng?,
    hasLocationPermission: Boolean,
    onRequestLocation: () -> Unit,
    onGoThere: (Parking) -> Unit
) {
    var selectedParking by remember {
        mutableStateOf<Parking?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: MapDhakaCenter,
            13f
        )
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    val visibleParkings = parkings.filter { parking ->
        parking.active &&
                parking.latitude != 0.0 &&
                parking.longitude != 0.0 &&
                (
                        parking.name.contains(searchQuery, true) ||
                                parking.address.contains(searchQuery, true)
                        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            ),
            onMapClick = {
                selectedParking = null
            }
        ) {
            visibleParkings.forEach { parking ->

                val position = LatLng(
                    parking.latitude,
                    parking.longitude
                )

                Marker(
                    state = remember(parking.id) {
                        MarkerState(position = position)
                    },
                    title = parking.name,
                    snippet = "${parking.availableSlots} slots • ৳${parking.pricePerHour}/hour",
                    onClick = {
                        selectedParking = parking
                        true
                    }
                )
            }
        }

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                selectedParking = null
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = {
                Text(
                    "Search parking facilities...",
                    color = MapSecondary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MapBlue
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MapSurface,
                unfocusedContainerColor = MapSurface,
                focusedTextColor = MapText,
                unfocusedTextColor = MapText,
                focusedBorderColor = MapBlue,
                unfocusedBorderColor = Color.Transparent
            )
        )

        // Current location button
        FloatingActionButton(
            onClick = onRequestLocation,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 92.dp, end = 16.dp),
            containerColor = MapSurface,
            contentColor = MapBlue
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "My location"
            )
        }

        // Location permission message
        if (!hasLocationPermission) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                color = MapSurface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Enable location",
                        color = MapText,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Allow location access to find parking near you.",
                        color = MapSecondary
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = onRequestLocation) {
                        Text("Enable Location")
                    }
                }
            }
        }

        // No parking facilities
        if (visibleParkings.isEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                color = MapSurface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (parkings.isEmpty()) {
                        "No active parking facilities available yet."
                    } else {
                        "No matching parking facilities found."
                    },
                    color = MapText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Selected parking details
        selectedParking?.let { parking ->

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 104.dp
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MapSurface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        parking.name,
                        color = MapText,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        parking.address,
                        color = MapSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${parking.availableSlots} slots available",
                            color = Color(0xFF22C55E)
                        )

                        Text(
                            "৳${parking.pricePerHour}/hour",
                            color = MapBlue,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { onGoThere(parking) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Go There")
                    }
                }
            }
        }
    }
}
