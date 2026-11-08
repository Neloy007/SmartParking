package com.example.smartparking.presentation.driver.utils

import com.example.smartparking.data.model.Parking
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlin.math.*

fun distanceInMeters(
    parking: Parking,
    currentLocation: LatLng
): Float {
    val earthRadius = 6_371_000.0

    val lat1 = Math.toRadians(currentLocation.latitude)
    val lat2 = Math.toRadians(parking.latitude)

    val deltaLat = Math.toRadians(
        parking.latitude - currentLocation.latitude
    )

    val deltaLng = Math.toRadians(
        parking.longitude - currentLocation.longitude
    )

    val a = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) *
            sin(deltaLng / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadius * c).toFloat()
}

fun formatDistance(meters: Float): String {
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