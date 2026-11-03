
package com.example.smartparking.presentation.owner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingRegistrationScreen(
    onBack: () -> Unit,
    ownerParkingViewModel: OwnerParkingViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by ownerParkingViewModel.uiState.collectAsState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var totalSlots by remember { mutableStateOf("20") }
    var availableSlots by remember { mutableStateOf("20") }
    var pricePerHour by remember { mutableStateOf("50") }

    // Default map location: Dhaka
    var selectedLocation by remember {
        mutableStateOf(LatLng(23.7808, 90.4142))
    }

    // Map camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(23.7808, 90.4142),
            13f
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val coroutineScope = rememberCoroutineScope()

    var locationMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Get the device's current location
    suspend fun updateCurrentLocation() {
        try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                val newLocation = LatLng(
                    location.latitude,
                    location.longitude
                )

                selectedLocation = newLocation

                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        newLocation,
                        17f
                    )
                )

                locationMessage = "Current location selected"
            } else {
                locationMessage =
                    "Unable to detect location. Try again."
            }

        } catch (e: SecurityException) {
            locationMessage = "Location permission is required."
        } catch (e: Exception) {
            locationMessage =
                e.message ?: "Unable to get current location."
        }
    }

    // Location permission launcher
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                coroutineScope.launch {
                    updateCurrentLocation()
                }
            } else {
                locationMessage = "Location permission was denied."
            }
        }

    // Register parking success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            ownerParkingViewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Register Parking")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                "Parking Facility Details",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Parking Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Full Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            HorizontalDivider()

            Text(
                "Select Parking Location",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Choose a location from the map or use your current location.",
                style = MaterialTheme.typography.bodySmall
            )

            // Location selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        locationMessage =
                            "Move the map and tap to select your parking location."
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Choose from map"
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Choose Map")
                }

                Button(
                    onClick = {
                        val fineGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        val coarseGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {
                            coroutineScope.launch {
                                updateCurrentLocation()
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Current location"
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("My Location")
                }
            }

            locationMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Google Maps
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    mapToolbarEnabled = true
                ),
                onMapClick = { location ->
                    selectedLocation = location
                    locationMessage = "Parking location selected."
                }
            ) {

                Marker(
                    state = remember(selectedLocation) {
                        MarkerState(position = selectedLocation)
                    },
                    title = name.ifBlank {
                        "Parking Facility"
                    },
                    snippet = address.ifBlank {
                        "Selected parking location"
                    }
                )
            }

            // Selected coordinates
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "Selected Location",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            name.ifBlank { "Parking Facility" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        "Latitude: ${selectedLocation.latitude}"
                    )

                    Text(
                        "Longitude: ${selectedLocation.longitude}"
                    )
                }
            }

            HorizontalDivider()

            Text(
                "Parking Capacity",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = totalSlots,
                onValueChange = { totalSlots = it },
                label = { Text("Total Parking Slots") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = availableSlots,
                onValueChange = { availableSlots = it },
                label = { Text("Available Slots") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = pricePerHour,
                onValueChange = { pricePerHour = it },
                label = { Text("Price Per Hour (BDT)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Register button
            Button(
                onClick = {
                    ownerParkingViewModel.registerParking(
                        name = name,
                        address = address,
                        latitude = selectedLocation.latitude,
                        longitude = selectedLocation.longitude,
                        totalSlots = totalSlots.toIntOrNull() ?: -1,
                        availableSlots = availableSlots.toIntOrNull() ?: -1,
                        pricePerHour = pricePerHour.toDoubleOrNull() ?: -1.0
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register Parking Facility")
                }
            }
        }
    }
}
