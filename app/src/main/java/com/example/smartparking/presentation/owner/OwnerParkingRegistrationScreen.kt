
package com.example.smartparking.presentation.owner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayWhite = Color(0xFFFFFFFF)
private val ParkwayBackground = Color(0xFFF7F7F5)
private val ParkwayGray = Color(0xFF777777)
private val ParkwayBorder = Color(0xFFEEEEEE)
private val ParkwayRed = Color(0xFFD94848)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerParkingRegistrationScreen(
    onBack: () -> Unit,
    ownerParkingViewModel: OwnerParkingViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by ownerParkingViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var totalSlots by remember { mutableStateOf("20") }
    var availableSlots by remember { mutableStateOf("20") }
    var pricePerHour by remember { mutableStateOf("50") }

    var selectedLocation by remember {
        mutableStateOf(LatLng(23.7808, 90.4142))
    }

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
                locationMessage = "Unable to detect location. Try again."
            }
        } catch (e: SecurityException) {
            locationMessage = "Location permission is required."
        } catch (e: Exception) {
            locationMessage = e.message ?: "Unable to get current location."
        }
    }

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

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            ownerParkingViewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        containerColor = ParkwayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "parkway",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ParkwayBlack,
                            letterSpacing = (-0.8).sp
                        )
                        Text(
                            "OWNER PORTAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParkwayGray,
                            letterSpacing = 2.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                            tint = ParkwayBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ParkwayWhite
                )
            )
        },
        bottomBar = {
            Surface(
                color = ParkwayWhite,
                shadowElevation = 12.dp
            ) {
                Button(
                    onClick = {
                        ownerParkingViewModel.registerParking(
                            name = name.trim(),
                            address = address.trim(),
                            latitude = selectedLocation.latitude,
                            longitude = selectedLocation.longitude,
                            totalSlots = totalSlots.toIntOrNull() ?: -1,
                            availableSlots = availableSlots.toIntOrNull() ?: -1,
                            pricePerHour = pricePerHour.toDoubleOrNull() ?: -1.0
                        )
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParkwayBlack,
                        contentColor = ParkwayWhite,
                        disabledContainerColor = ParkwayGray
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ParkwayLime,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Register Parking Facility",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ParkwayLime
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // Header
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(ParkwayLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = ParkwayBlack,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Text(
                    "List your parking.",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ParkwayBlack,
                    letterSpacing = (-0.8).sp
                )

                Text(
                    "Add your facility and start managing your parking spaces.",
                    fontSize = 14.sp,
                    color = ParkwayGray,
                    lineHeight = 21.sp
                )
            }

            // Facility details
            Column(
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SectionHeading(
                    number = "01",
                    title = "Facility details",
                    subtitle = "Tell us about your parking facility"
                )

                ParkwayTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Parking facility name",
                    placeholder = "e.g. Dhanmondi Parking Center"
                )

                ParkwayTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Full address",
                    placeholder = "Street, area, city",
                    minLines = 2
                )
            }

            // Location
            Column(
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SectionHeading(
                    number = "02",
                    title = "Parking location",
                    subtitle = "Pinpoint your facility on the map"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            locationMessage =
                                "Move the map and tap anywhere to select your parking location."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ParkwayBlack
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            ParkwayBorder
                        )
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Choose map")
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkwayBlack,
                            contentColor = ParkwayWhite
                        )
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = ParkwayLime
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("My location")
                    }
                }

                locationMessage?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = ParkwayGray
                    )
                }

                // Map
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .border(
                            1.dp,
                            ParkwayBorder,
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ParkwayWhite
                    )
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
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
                            title = name.ifBlank { "Parking Facility" },
                            snippet = address.ifBlank {
                                "Selected parking location"
                            }
                        )
                    }
                }

                // Coordinates
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ParkwayWhite
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ParkwayLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ParkwayBlack
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                "Selected coordinates",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ParkwayBlack
                            )
                            Text(
                                "${selectedLocation.latitude}, ${selectedLocation.longitude}",
                                fontSize = 11.sp,
                                color = ParkwayGray
                            )
                        }
                    }
                }
            }

            // Capacity
            Column(
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SectionHeading(
                    number = "03",
                    title = "Parking capacity",
                    subtitle = "Set your available parking spaces"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ParkwayTextField(
                        value = totalSlots,
                        onValueChange = { totalSlots = it },
                        label = "Total slots",
                        placeholder = "20",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )

                    ParkwayTextField(
                        value = availableSlots,
                        onValueChange = { availableSlots = it },
                        label = "Available slots",
                        placeholder = "20",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                }

                ParkwayTextField(
                    value = pricePerHour,
                    onValueChange = { pricePerHour = it },
                    label = "Price per hour (BDT)",
                    placeholder = "50",
                    keyboardType = KeyboardType.Decimal,
                    leadingContent = {
                        Text(
                            "৳",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParkwayBlack
                        )
                    }
                )

                // Price note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF5E5))
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = ParkwayBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "This is your base hourly parking rate.",
                        fontSize = 12.sp,
                        color = ParkwayBlack
                    )
                }
            }

            // Validation / API error
            uiState.error?.let { error ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEEEE)
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(14.dp),
                        color = ParkwayRed,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionHeading(
    number: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ParkwayLime),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ParkwayBlack
            )
        }

        Spacer(Modifier.width(11.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ParkwayBlack
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = ParkwayGray
            )
        }
    }
}

@Composable
private fun ParkwayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ParkwayBlack
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )
            },
            leadingIcon = leadingContent,
            minLines = minLines,
            singleLine = minLines == 1,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ParkwayBlack,
                unfocusedBorderColor = ParkwayBorder,
                focusedContainerColor = ParkwayWhite,
                unfocusedContainerColor = ParkwayWhite,
                focusedTextColor = ParkwayBlack,
                unfocusedTextColor = ParkwayBlack,
                cursorColor = ParkwayBlack
            )
        )
    }
}
