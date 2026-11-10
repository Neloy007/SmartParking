
package com.example.smartparking.presentation.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartparking.data.model.Parking
import com.example.smartparking.presentation.driver.components.*
import com.example.smartparking.presentation.driver.utils.distanceInMeters
import com.example.smartparking.presentation.driver.utils.formatDistance
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun DriverHomeScreen(
    onLogout: () -> Unit
) {

    val context = LocalContext.current

    val parkingViewModel: DriverParkingViewModel = viewModel()
    val uiState by parkingViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedParking by remember {
        mutableStateOf<Parking?>(null)
    }

    // Parking selected for scheduled booking
    var bookingParking by remember {
        mutableStateOf<Parking?>(null)
    }

    var demoBookings by remember {
        mutableStateOf<List<DemoParkingBooking>>(emptyList())
    }

    var activeNavigationParking by remember {
        mutableStateOf<Parking?>(null)
    }

    var pendingNavigationParking by remember {
        mutableStateOf<Parking?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

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

    val coroutineScope = rememberCoroutineScope()

    suspend fun refreshCurrentLocation() {
        try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                currentLocation = LatLng(
                    location.latitude,
                    location.longitude
                )
            }
        } catch (e: SecurityException) {
            hasLocationPermission = false
        } catch (e: Exception) {
            // Location could not be retrieved.
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            hasLocationPermission = granted

            if (granted) {
                pendingNavigationParking?.let { parking ->
                    coroutineScope.launch {
                        refreshCurrentLocation()
                        activeNavigationParking = parking
                        pendingNavigationParking = null
                    }
                }
            } else {
                pendingNavigationParking = null
            }
        }

    // Load parking records
    LaunchedEffect(Unit) {
        parkingViewModel.loadParkings()
    }

    // Refresh location when permission is available
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            refreshCurrentLocation()
        }
    }

    val requestLocation: () -> Unit = {
        if (hasLocationPermission) {
            coroutineScope.launch {
                refreshCurrentLocation()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Filter parking spaces
    val filteredParkings = remember(
        uiState.parkings,
        searchQuery,
        selectedCategory,
        currentLocation
    ) {
        val filtered = uiState.parkings.filter { parking ->

            val matchesSearch =
                parking.name.contains(searchQuery, ignoreCase = true) ||
                        parking.address.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "Affordable" -> parking.pricePerHour <= 50.0

                "Nearby" -> currentLocation?.let {
                    distanceInMeters(parking, it) <= 5000
                } ?: true

                else -> true
            }

            matchesSearch && matchesCategory
        }

        if (currentLocation != null) {
            filtered.sortedBy {
                distanceInMeters(it, currentLocation!!)
            }
        } else {
            filtered
        }
    }

    // Start in-app navigation
    val startNavigation: (Parking) -> Unit = { parking ->

        if (hasLocationPermission) {
            coroutineScope.launch {
                refreshCurrentLocation()
                activeNavigationParking = parking
            }
        } else {
            pendingNavigationParking = parking
            requestLocation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ------------------------------------------------
        // BOOKING SCREEN
        // ------------------------------------------------

        if (bookingParking != null) {

            BookingScreen(
                parking = bookingParking!!,
                onBack = {
                    bookingParking = null
                },
                onBookingCreated = { booking ->

                    demoBookings = listOf(booking) + demoBookings

                    bookingParking = null
                    selectedTab = 1
                }
            )

        }

        // ------------------------------------------------
        // NAVIGATION SCREEN
        // ------------------------------------------------

        else if (activeNavigationParking != null) {

            val destinationParking = activeNavigationParking!!

            DriverNavigationScreen(
                destination = LatLng(
                    destinationParking.latitude,
                    destinationParking.longitude
                ),
                parkingName = destinationParking.name,
                parkingId = destinationParking.id,
                hourlyRate = destinationParking.pricePerHour,
                hasLocationPermission = hasLocationPermission,
                onBack = {
                    activeNavigationParking = null
                }
            )

        }

        // ------------------------------------------------
        // MAIN DRIVER SCREENS
        // ------------------------------------------------

        else {

            when (selectedTab) {

                // HOME
                0 -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        item {
                            DriverHeader(
                                onRefresh = {
                                    parkingViewModel.refreshParkings()
                                    requestLocation()
                                }
                            )
                        }

                        // Current location
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = Color(0xFF16A34A)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = "Your current location",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )

                                        Text(
                                            text = if (currentLocation != null) {
                                                "Location detected"
                                            } else {
                                                "Location unavailable"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    TextButton(
                                        onClick = requestLocation
                                    ) {
                                        Text("Refresh")
                                    }
                                }
                            }
                        }

                        // Heading
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Text(
                                    text = "Find Your Perfect Parking",
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Text(
                                    text = "Discover available parking spaces near you.",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Search
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Search parking by name or location")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF16A34A),
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        // Filters
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                Text(
                                    text = "Explore parking",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    FilterChip(
                                        selected = selectedCategory == "All",
                                        onClick = {
                                            selectedCategory = "All"
                                        },
                                        label = {
                                            Text("All")
                                        }
                                    )

                                    FilterChip(
                                        selected = selectedCategory == "Nearby",
                                        onClick = {
                                            selectedCategory = "Nearby"
                                        },
                                        label = {
                                            Text("Nearby")
                                        }
                                    )

                                    FilterChip(
                                        selected = selectedCategory == "Affordable",
                                        onClick = {
                                            selectedCategory = "Affordable"
                                        },
                                        label = {
                                            Text("Affordable")
                                        }
                                    )
                                }
                            }
                        }

                        // Parking count
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column {

                                    Text(
                                        text = "Parking near you",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )

                                    Text(
                                        text = "${filteredParkings.size} parking spaces found",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = "Parking",
                                    tint = Color(0xFF16A34A)
                                )
                            }
                        }

                        // Loading
                        if (uiState.isLoading) {

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }

                        }

                        // Error
                        else if (uiState.error != null) {

                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(36.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = uiState.error
                                            ?: "Something went wrong",
                                        color = Color(0xFFDC2626),
                                        textAlign = TextAlign.Center
                                    )

                                    TextButton(
                                        onClick = {
                                            parkingViewModel.refreshParkings()
                                        }
                                    ) {
                                        Text("Try again")
                                    }
                                }
                            }

                        }

                        // Empty state
                        else if (filteredParkings.isEmpty()) {

                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.LocalParking,
                                        contentDescription = "No parking",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(42.dp)
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = "No parking spaces found",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "Try another search or change your filter.",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                        }

                        // Parking list
                        else {

                            items(
                                items = filteredParkings,
                                key = { it.id }
                            ) { parking ->

                                val distance = currentLocation?.let {
                                    formatDistance(
                                        distanceInMeters(parking, it)
                                    )
                                } ?: "Unavailable"

                                ParkingCard(
                                    parking = parking,
                                    distance = distance,
                                    onClick = {
                                        selectedParking = parking
                                    },
                                    onNavigate = {
                                        startNavigation(parking)
                                    }
                                )
                            }
                        }
                    }
                }

                // BOOKINGS
                1 -> {
                    MyBookingsScreen(
                        bookings = demoBookings
                    )
                }

                // MAP
                2 -> {
                    ParkingMap(
                        parkings = uiState.parkings,
                        currentLocation = currentLocation,
                        hasLocationPermission = hasLocationPermission,
                        onRequestLocation = requestLocation,
                        onGoThere = { parking ->
                            startNavigation(parking)
                        }
                    )
                }

                // SAVED
                3 -> {
                    PlaceholderDriverTab(tab = 3)
                }

// PROFILE
                4 -> {
                    DriverProfileScreen(
                        onLogout = onLogout
                    )
                }

                else -> {
                    PlaceholderDriverTab(tab = selectedTab)
                }
            }

            // Bottom navigation
            FloatingDriverNavigation(
                selectedTab = selectedTab,
                onSelect = {
                    selectedTab = it
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    )
            )
        }

        // ------------------------------------------------
        // PARKING DETAILS DIALOG
        // ------------------------------------------------

        selectedParking?.let { parking ->

            val distance = currentLocation?.let {
                formatDistance(
                    distanceInMeters(parking, it)
                )
            } ?: "Unavailable"

            ParkingDetailsDialog(
                parking = parking,
                distance = distance,

                onDismiss = {
                    selectedParking = null
                },

                onGoThere = {
                    selectedParking = null
                    startNavigation(parking)
                },

                onBookParking = {
                    selectedParking = null
                    bookingParking = parking
                }
            )
        }
    }
}
