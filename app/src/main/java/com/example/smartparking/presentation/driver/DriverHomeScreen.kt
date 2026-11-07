
package com.example.smartparking.presentation.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.smartparking.data.model.Parking
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import java.util.Locale

// ----------------------------------------------------
// PARKWAY BRAND COLORS
// ----------------------------------------------------

private val Background = Color(0xFFFFFFFF)
private val SurfaceColor = Color(0xFFFFFFFF)

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayWhite = Color(0xFFFFFFFF)
private val ParkwayLime = Color(0xFFB8F34A)

private val MainText = Color(0xFF080808)
private val SecondaryText = Color(0xFF777777)

private val ParkwayInput = Color(0xFFF4F4F4)
private val ParkwayBorder = Color(0xFFEEEEEE)

private val Green = Color(0xFFB8F34A)
private val ErrorColor = Color(0xFFD32F2F)


// ----------------------------------------------------
// DRIVER HOME SCREEN
// ----------------------------------------------------

@Composable
fun DriverHomeScreen() {

    val context = LocalContext.current

    val parkingViewModel: DriverParkingViewModel = viewModel()
    val uiState by parkingViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedParking by remember {
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


    // ------------------------------------------------
    // LOCATION REFRESH
    // ------------------------------------------------

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

            // Location unavailable.

        }
    }


    // ------------------------------------------------
    // LOCATION PERMISSION
    // ------------------------------------------------

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }


    // ------------------------------------------------
    // INITIAL LOAD
    // ------------------------------------------------

    LaunchedEffect(Unit) {
        parkingViewModel.loadParkings()
    }

    LaunchedEffect(hasLocationPermission) {

        if (hasLocationPermission) {
            refreshCurrentLocation()
        }
    }


    // ------------------------------------------------
    // REQUEST LOCATION
    // ------------------------------------------------

    val requestLocation = {

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


    // ------------------------------------------------
    // FILTER PARKING
    // ------------------------------------------------

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


    // ------------------------------------------------
    // MAIN SCREEN
    // ------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        when (selectedTab) {

            // ========================================
            // HOME TAB
            // ========================================

            0 -> {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 26.dp,
                        bottom = 115.dp
                    ),

                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {

                    // HEADER

                    item {

                        DriverHeader(
                            onRefresh = {
                                parkingViewModel.loadParkings()
                            }
                        )
                    }


                    // LOCATION

                    item {

                        Column {

                            Text(
                                text = "YOUR CURRENT LOCATION",
                                color = SecondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Surface(
                                    shape = CircleShape,
                                    color = ParkwayInput,
                                    modifier = Modifier.size(42.dp)
                                ) {

                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = ParkwayBlack
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = if (currentLocation != null) {
                                            "Location detected"
                                        } else {
                                            "Location unavailable"
                                        },
                                        color = MainText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = if (currentLocation != null) {
                                            "Ready to find parking nearby"
                                        } else {
                                            "Enable location to find nearby parking"
                                        },
                                        color = SecondaryText,
                                        fontSize = 11.sp
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        requestLocation()
                                        Unit
                                    }
                                ) {

                                    Text(
                                        "Update",
                                        color = ParkwayBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }


                    // SEARCH

                    item {

                        OutlinedTextField(
                            value = searchQuery,

                            onValueChange = {
                                searchQuery = it
                            },

                            modifier = Modifier.fillMaxWidth(),

                            placeholder = {

                                Text(
                                    "Where do you want to park?",
                                    color = SecondaryText,
                                    fontSize = 13.sp
                                )
                            },

                            leadingIcon = {

                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = ParkwayBlack
                                )
                            },

                            trailingIcon = {

                                if (searchQuery.isNotEmpty()) {

                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                        }
                                    ) {

                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = SecondaryText
                                        )
                                    }
                                }
                            },

                            singleLine = true,

                            shape = RoundedCornerShape(15.dp),

                            colors = OutlinedTextFieldDefaults.colors(

                                focusedContainerColor = ParkwayInput,
                                unfocusedContainerColor = ParkwayInput,

                                focusedTextColor = MainText,
                                unfocusedTextColor = MainText,

                                cursorColor = ParkwayBlack,

                                focusedBorderColor = ParkwayBlack,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }


                    // HERO TEXT

                    item {

                        Column {

                            Text(
                                text = "Find your parking.",
                                color = MainText,
                                fontSize = 27.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.8).sp
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Your space. Your time. Your way.",
                                color = SecondaryText,
                                fontSize = 13.sp
                            )
                        }
                    }


                    // FILTER CHIPS

                    item {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {

                            listOf(
                                "All",
                                "Nearby",
                                "Affordable"
                            ).forEach { category ->

                                FilterChip(
                                    selected = selectedCategory == category,

                                    onClick = {
                                        selectedCategory = category
                                    },

                                    label = {

                                        Text(
                                            category,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },

                                    shape = RoundedCornerShape(30.dp),

                                    colors = FilterChipDefaults.filterChipColors(

                                        selectedContainerColor = ParkwayBlack,
                                        selectedLabelColor = ParkwayWhite,

                                        containerColor = ParkwayInput,
                                        labelColor = SecondaryText
                                    ),

                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCategory == category,
                                        borderColor = ParkwayBorder,
                                        selectedBorderColor = ParkwayBlack
                                    )
                                )
                            }
                        }
                    }


                    // PARKING SECTION HEADER

                    item {

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween,

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text = "Parking near you",
                                    color = MainText,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Explore available parking spaces",
                                    color = SecondaryText,
                                    fontSize = 11.sp
                                )
                            }

                            Surface(
                                color = ParkwayInput,
                                shape = RoundedCornerShape(20.dp)
                            ) {

                                Text(
                                    text = "${filteredParkings.size} spots",
                                    color = ParkwayBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    )
                                )
                            }
                        }
                    }


                    // LOADING STATE

                    if (uiState.isLoading) {

                        item {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),

                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                CircularProgressIndicator(
                                    color = ParkwayBlack,
                                    strokeWidth = 3.dp
                                )

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    "Finding parking spaces...",
                                    color = SecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }


                    // ERROR STATE

                    else if (uiState.error != null) {

                        item {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 25.dp),

                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorColor,
                                    modifier = Modifier.size(42.dp)
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    uiState.error ?: "Unable to load parking",
                                    color = ErrorColor,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        parkingViewModel.loadParkings()
                                    },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ParkwayBlack
                                    )
                                ) {

                                    Text("Try Again")
                                }
                            }
                        }
                    }


                    // EMPTY STATE

                    else if (filteredParkings.isEmpty()) {

                        item {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 35.dp),

                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Surface(
                                    shape = CircleShape,
                                    color = ParkwayInput,
                                    modifier = Modifier.size(76.dp)
                                ) {

                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Icon(
                                            Icons.Default.LocalParking,
                                            contentDescription = null,
                                            tint = ParkwayBlack,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "No parking spaces found",
                                    color = MainText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Try another search or check back later.",
                                    color = SecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }


                    // PARKING LIST

                    else {

                        items(
                            items = filteredParkings,
                            key = { it.id }
                        ) { parking ->

                            ParkingCard(
                                parking = parking,

                                distance = currentLocation?.let {
                                    formatDistance(
                                        distanceInMeters(parking, it)
                                    )
                                } ?: "Distance unavailable",

                                onClick = {
                                    selectedParking = parking
                                }
                            )
                        }
                    }
                }
            }


            // ========================================
            // NEARBY MAP TAB
            // ========================================

            2 -> {

                ParkingMap(
                    parkings = uiState.parkings,

                    currentLocation = currentLocation,

                    hasLocationPermission = hasLocationPermission,

                    onRequestLocation = {
                        requestLocation()
                        Unit
                    },

                    onGoThere = { parking ->
                        openGoogleMaps(context, parking)
                        Unit
                    }
                )
            }


            // ========================================
            // OTHER TABS
            // ========================================

            else -> {

                PlaceholderDriverTab(
                    tab = selectedTab
                )
            }
        }


        // ============================================
        // FLOATING BOTTOM NAVIGATION
        // ============================================

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


    // ================================================
    // PARKING DETAILS DIALOG
    // ================================================

    selectedParking?.let { parking ->

        ParkingDetailsDialog(
            parking = parking,

            distance = currentLocation?.let {
                formatDistance(distanceInMeters(parking, it))
            } ?: "Unavailable",

            onDismiss = {
                selectedParking = null
            },

            onGoThere = {
                selectedParking = null
                openGoogleMaps(context, parking)
            }
        )
    }
}


// ----------------------------------------------------
// DISTANCE CALCULATION
// ----------------------------------------------------

private fun distanceInMeters(
    parking: Parking,
    currentLocation: LatLng
): Float {

    val result = FloatArray(1)

    Location.distanceBetween(
        currentLocation.latitude,
        currentLocation.longitude,
        parking.latitude,
        parking.longitude,
        result
    )

    return result[0]
}


// ----------------------------------------------------
// DISTANCE FORMAT
// ----------------------------------------------------

private fun formatDistance(meters: Float): String {

    return if (meters < 1000) {

        "${meters.toInt()} m away"

    } else {

        String.format(
            Locale.US,
            "%.1f km away",
            meters / 1000
        )
    }
}


// ----------------------------------------------------
// GOOGLE MAPS NAVIGATION
// ----------------------------------------------------

private fun openGoogleMaps(
    context: Context,
    parking: Parking
) {

    val navigationUri = Uri.parse(
        "google.navigation:q=${parking.latitude},${parking.longitude}&mode=d"
    )

    val googleMapsIntent = Intent(
        Intent.ACTION_VIEW,
        navigationUri
    ).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {

        context.startActivity(googleMapsIntent)

    } catch (e: Exception) {

        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "geo:${parking.latitude},${parking.longitude}?q=${parking.latitude},${parking.longitude}"
            )
        )

        try {

            context.startActivity(fallbackIntent)

        } catch (ignored: Exception) {

            // No compatible map application installed.
        }
    }
}


// ----------------------------------------------------
// PARKWAY HEADER
// ----------------------------------------------------

@Composable
private fun DriverHeader(
    onRefresh: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // PARKWAY LOGO

            Surface(
                modifier = Modifier.size(48.dp),

                shape = RoundedCornerShape(14.dp),

                color = ParkwayBlack
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "P",
                        color = ParkwayLime,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {

                Text(
                    text = "parkway",
                    color = ParkwayBlack,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "PARK SMARTER. LIVE BETTER.",
                    color = SecondaryText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }


        // REFRESH BUTTON

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onRefresh),

            shape = CircleShape,

            color = ParkwayInput
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh parking",
                    tint = ParkwayBlack
                )
            }
        }
    }
}


// ----------------------------------------------------
// PARKING CARD
// ----------------------------------------------------

@Composable
private fun ParkingCard(
    parking: Parking,
    distance: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        ),

        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ParkwayBorder
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp)
        ) {

            // TOP ROW

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    color = ParkwayInput,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(46.dp)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = ParkwayBlack,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(Modifier.width(3.dp))

                    Text(
                        text = distance,
                        color = SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            Spacer(Modifier.height(15.dp))


            // PARKING NAME

            Text(
                text = parking.name,
                color = MainText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))


            // ADDRESS

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = parking.address,
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }


            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                color = ParkwayBorder,
                thickness = 1.dp
            )

            Spacer(Modifier.height(14.dp))


            // PRICE AND AVAILABILITY

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {

                        Text(
                            text = "৳${parking.pricePerHour}",
                            color = ParkwayBlack,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = "/ hour",
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(7.dp),
                            shape = CircleShape,
                            color = Green
                        ) {}

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "${parking.availableSlots} slots available",
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                }


                // VIEW DETAILS BUTTON

                Button(
                    onClick = onClick,

                    shape = RoundedCornerShape(12.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParkwayBlack,
                        contentColor = ParkwayWhite
                    ),

                    contentPadding = PaddingValues(
                        horizontal = 17.dp,
                        vertical = 12.dp
                    )
                ) {

                    Text(
                        text = "View details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(5.dp))

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}


// ----------------------------------------------------
// PARKING DETAILS DIALOG
// ----------------------------------------------------

@Composable
private fun ParkingDetailsDialog(
    parking: Parking,
    distance: String,
    onDismiss: () -> Unit,
    onGoThere: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        containerColor = ParkwayWhite,

        titleContentColor = ParkwayBlack,

        textContentColor = SecondaryText,

        shape = RoundedCornerShape(24.dp),

        title = {

            Column {

                Surface(
                    color = ParkwayInput,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = ParkwayBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                Text(
                    text = parking.name,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = ParkwayBlack
                )
            }
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = parking.address
                )

                DetailRow(
                    icon = Icons.Default.NearMe,
                    label = "Distance",
                    value = distance
                )

                DetailRow(
                    icon = Icons.Default.EventAvailable,
                    label = "Available slots",
                    value = "${parking.availableSlots}"
                )

                DetailRow(
                    icon = Icons.Default.LocalParking,
                    label = "Total slots",
                    value = "${parking.totalSlots}"
                )

                DetailRow(
                    icon = Icons.Default.Payments,
                    label = "Hourly price",
                    value = "৳${parking.pricePerHour}/hour"
                )
            }
        },

        confirmButton = {

            Button(
                onClick = onGoThere,

                shape = RoundedCornerShape(12.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkwayBlack,
                    contentColor = ParkwayWhite
                )
            ) {

                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null
                )

                Spacer(Modifier.width(7.dp))

                Text(
                    "Navigate",
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    "Close",
                    color = SecondaryText
                )
            }
        }
    )
}


// ----------------------------------------------------
// PARKING DETAILS ROW
// ----------------------------------------------------

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            color = ParkwayInput,
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.size(36.dp)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = ParkwayBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(11.dp))

        Column {

            Text(
                text = label,
                color = SecondaryText,
                fontSize = 10.sp
            )

            Text(
                text = value,
                color = ParkwayBlack,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// ----------------------------------------------------
// FLOATING DRIVER NAVIGATION
// ----------------------------------------------------

@Composable
private fun FloatingDriverNavigation(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),

        shape = RoundedCornerShape(23.dp),

        color = ParkwayBlack,

        shadowElevation = 14.dp
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),

            verticalAlignment = Alignment.CenterVertically
        ) {

            DriverNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(0) }
            )

            DriverNavItem(
                label = "Bookings",
                icon = Icons.Default.CalendarMonth,
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(1) }
            )


            // CENTER MAP BUTTON

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        onSelect(2)
                    },

                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Surface(
                        shape = CircleShape,
                        color = ParkwayLime,
                        modifier = Modifier.size(39.dp)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                Icons.Default.Map,
                                contentDescription = "Nearby",
                                tint = ParkwayBlack,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = "Nearby",
                        color = if (selectedTab == 2) {
                            ParkwayLime
                        } else {
                            Color.White.copy(alpha = 0.65f)
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            DriverNavItem(
                label = "Saved",
                icon = Icons.Default.Favorite,
                selected = selectedTab == 3,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(3) }
            )

            DriverNavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                selected = selectedTab == 4,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(4) }
            )
        }
    }
}


// ----------------------------------------------------
// NAVIGATION ITEM
// ----------------------------------------------------

@Composable
private fun DriverNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,

            tint = if (selected) {
                ParkwayLime
            } else {
                Color.White.copy(alpha = 0.65f)
            },

            modifier = Modifier.size(21.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,

            color = if (selected) {
                ParkwayLime
            } else {
                Color.White.copy(alpha = 0.65f)
            },

            fontSize = 9.sp,

            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}


// ----------------------------------------------------
// PLACEHOLDER TABS
// ----------------------------------------------------

@Composable
private fun PlaceholderDriverTab(
    tab: Int
) {

    val title = when (tab) {

        1 -> "My Bookings"

        3 -> "Saved Parking"

        4 -> "My Profile"

        else -> "Parkway"
    }

    val subtitle = when (tab) {

        1 -> "Your parking reservations will appear here."

        3 -> "Your favourite parking spaces will appear here."

        4 -> "Manage your personal account information."

        else -> "Your smart parking experience."
    }

    val icon = when (tab) {

        1 -> Icons.Default.CalendarMonth

        3 -> Icons.Default.Favorite

        4 -> Icons.Default.Person

        else -> Icons.Default.DirectionsCar
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(bottom = 100.dp),

        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,

            modifier = Modifier.padding(30.dp)
        ) {

            Surface(
                color = ParkwayInput,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(90.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        icon,
                        contentDescription = null,
                        tint = ParkwayBlack,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                color = ParkwayBlack,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = subtitle,
                color = SecondaryText,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                color = ParkwayLime,
                shape = RoundedCornerShape(30.dp)
            ) {

                Text(
                    text = "COMING SOON",
                    color = ParkwayBlack,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(
                        horizontal = 17.dp,
                        vertical = 9.dp
                    )
                )
            }
        }
    }
}
