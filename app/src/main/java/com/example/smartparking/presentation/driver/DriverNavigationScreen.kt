
package com.example.smartparking.presentation.driver

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.location.Location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.example.smartparking.data.repository.NavigationRepository
import com.example.smartparking.data.repository.ParkingSessionRepository

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng

import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import java.util.Locale

import android.graphics.BitmapFactory
import com.example.smartparking.R

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverNavigationScreen(
    destination: LatLng,
    parkingName: String,
    parkingId: String,
    hourlyRate: Double,
    hasLocationPermission: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val navigationRepository = remember {
        NavigationRepository()
    }

    val sessionRepository = remember {
        ParkingSessionRepository()
    }

    val scope = rememberCoroutineScope()

    var routeJob by remember {
        mutableStateOf<Job?>(null)
    }

    var routeRequestId by remember {
        mutableIntStateOf(0)
    }

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    var routePoints by remember {
        mutableStateOf<List<LatLng>>(emptyList())
    }

    var distance by remember {
        mutableDoubleStateOf(0.0)
    }

    var duration by remember {
        mutableDoubleStateOf(0.0)
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var currentBearing by remember {
        mutableFloatStateOf(0f)
    }

    var arrived by remember {
        mutableStateOf(false)
    }

    var arrivalCount by remember {
        mutableIntStateOf(0)
    }

    var creatingSession by remember {
        mutableStateOf(false)
    }

    var sessionId by remember {
        mutableStateOf<String?>(null)
    }

    val cameraState = rememberCameraPositionState()

    val destinationMarkerState = rememberMarkerState(
        position = destination
    )

    val carMarkerState = rememberMarkerState(
        position = destination
    )

    /*
     * IMPORTANT:
     * Do not create the BitmapDescriptor during composition.
     * Google Maps must initialize its descriptor factory first.
     */
    var carIcon by remember {
        mutableStateOf<BitmapDescriptor?>(null)
    }

    LaunchedEffect(destination) {
        destinationMarkerState.position = destination
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { position ->

            carMarkerState.position = position

            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    position,
                    17f
                ),
                durationMs = 500
            )
        }
    }

    // Load or refresh the road route.
    fun loadRoute(origin: LatLng) {
        routeRequestId++

        val requestId = routeRequestId

        routeJob?.cancel()

        routeJob = scope.launch {
            loading = true
            error = null

            try {
                val result = navigationRepository.getRoute(
                    origin = origin,
                    destination = destination
                )

                if (requestId == routeRequestId) {
                    routePoints = result.points
                    distance = result.distanceMeters
                    duration = result.durationSeconds
                }

            } catch (e: Exception) {

                if (
                    e !is CancellationException &&
                    requestId == routeRequestId
                ) {
                    error = e.message
                        ?: "Unable to load route."
                }

            } finally {

                if (requestId == routeRequestId) {
                    loading = false
                }
            }
        }
    }

    // Listen for live location updates.
    DisposableEffect(
        hasLocationPermission,
        destination
    ) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {

            var lastRouteLocation: LatLng? = null
            var previousLocation: Location? = null

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            )
                .setMinUpdateDistanceMeters(3f)
                .build()

            val callback = object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {
                    val location = result.lastLocation ?: return

                    val newPosition = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    currentLocation = newPosition

                    // Calculate direction of travel.
                    val previous = previousLocation

                    if (
                        location.hasBearing() &&
                        location.speed > 1f
                    ) {
                        currentBearing = location.bearing

                    } else if (previous != null) {

                        val calculatedBearing =
                            previous.bearingTo(location)

                        if (calculatedBearing >= 0f) {
                            currentBearing = calculatedBearing
                        }
                    }

                    previousLocation = location

                    // Check whether the driver is near the destination.
                    val distanceToDestination = FloatArray(1)

                    Location.distanceBetween(
                        location.latitude,
                        location.longitude,
                        destination.latitude,
                        destination.longitude,
                        distanceToDestination
                    )

                    val accurateEnough = location.accuracy <= 50f

                    val arrivalRadius = maxOf(
                        35f,
                        location.accuracy
                    )

                    if (
                        !arrived &&
                        accurateEnough &&
                        distanceToDestination[0] <= arrivalRadius
                    ) {
                        arrivalCount++

                        // Require two qualifying location updates.
                        if (arrivalCount >= 2) {
                            arrived = true
                            routeJob?.cancel()
                        }

                    } else if (!arrived) {
                        arrivalCount = 0
                    }

                    // Refresh the route after moving 100 metres.
                    if (!arrived) {

                        val previousRoutePosition =
                            lastRouteLocation

                        if (previousRoutePosition == null) {

                            lastRouteLocation = newPosition
                            loadRoute(newPosition)

                        } else {

                            val moved = FloatArray(1)

                            Location.distanceBetween(
                                previousRoutePosition.latitude,
                                previousRoutePosition.longitude,
                                newPosition.latitude,
                                newPosition.longitude,
                                moved
                            )

                            if (moved[0] >= 100f) {
                                lastRouteLocation = newPosition
                                loadRoute(newPosition)
                            }
                        }
                    }
                }
            }

            try {
                fusedClient.requestLocationUpdates(
                    request,
                    callback,
                    context.mainLooper
                )
            } catch (e: SecurityException) {
                error = "Location permission is required."
            }

            onDispose {
                routeJob?.cancel()
                fusedClient.removeLocationUpdates(callback)
            }
        }
    }

    // Show payment screen after creating a parking session.
    val activeSessionId = sessionId

    if (activeSessionId != null) {

        PaymentScreen(
            sessionId = activeSessionId,
            parkingName = parkingName,
            hourlyRate = hourlyRate,
            onDone = onBack
        )

    } else {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Parkway Navigation")

                            Text(
                                text = parkingName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },

            bottomBar = {
                Surface(
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        when {

                            arrived -> {

                                Text(
                                    text = "You've reached your destination!",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF16A34A)
                                )

                                Text(
                                    text = "You are near $parkingName."
                                )

                                Button(
                                    onClick = {
                                        scope.launch {
                                            creatingSession = true
                                            error = null

                                            try {
                                                sessionId =
                                                    sessionRepository.createPendingSession(
                                                        parkingId = parkingId,
                                                        parkingName = parkingName,
                                                        hourlyRate = hourlyRate
                                                    )

                                            } catch (e: Exception) {
                                                error = e.message
                                                    ?: "Unable to create parking session."
                                            } finally {
                                                creatingSession = false
                                            }
                                        }
                                    },
                                    enabled = !creatingSession,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                ) {
                                    if (creatingSession) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Park Now")
                                    }
                                }
                            }

                            loading -> {

                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("Finding your route...")
                            }

                            routePoints.isNotEmpty() -> {

                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.1f km route",
                                        distance / 1000
                                    ),
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = "Estimated time: ${(duration / 60).toInt()} minutes"
                                )
                            }
                        }

                        error?.let { message ->

                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(
                                onClick = {
                                    currentLocation?.let(::loadRoute)
                                }
                            ) {
                                Text("Retry")
                            }
                        }

                        if (!arrived) {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Stop Navigation")
                            }
                        }
                    }
                }
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                if (hasLocationPermission) {

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,

                        uiSettings = MapUiSettings(
                            myLocationButtonEnabled = true,
                            zoomControlsEnabled = true,
                            compassEnabled = true
                        ),

                        // Create the custom marker only after Maps loads.
                        onMapLoaded = {
                            if (carIcon == null) {
                                carIcon = createCarMarkerIcon(context)
                            }
                        }
                    ) {

                        Marker(
                            state = destinationMarkerState,
                            title = parkingName,
                            snippet = "Parking destination"
                        )

                        if (routePoints.isNotEmpty()) {
                            Polyline(
                                points = routePoints,
                                color = Color(0xFF2563EB),
                                width = 12f
                            )
                        }

                        if (
                            currentLocation != null &&
                            carIcon != null
                        ) {
                            Marker(
                                state = carMarkerState,
                                title = "Your car",
                                icon = carIcon!!,
                                rotation = currentBearing,
                                flat = true,
                                anchor = Offset(0.5f, 0.5f)
                            )
                        }
                    }

                    if (currentLocation == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                } else {

                    Text(
                        text = "Location permission is required.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Creates a custom car-shaped map marker.
 *
 * The front of the car points toward the top
 * of the bitmap. The marker rotates using GPS bearing.
 */
private fun createCarMarkerIcon(
    context: android.content.Context
): BitmapDescriptor {

    val originalBitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.car_marker
    )

    // Resize the car icon to 64 × 64 pixels.
    val resizedBitmap = Bitmap.createScaledBitmap(
        originalBitmap,
        80,
        80,
        true
    )

    originalBitmap.recycle()

    return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
}