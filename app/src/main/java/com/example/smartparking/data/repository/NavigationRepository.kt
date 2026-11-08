package com.example.smartparking.data.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class NavigationRoute(
    val points: List<LatLng>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

class NavigationRepository {

    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng
    ): NavigationRoute = withContext(Dispatchers.IO) {

        // Validate coordinates
        require(
            origin.latitude in -90.0..90.0 &&
                    origin.longitude in -180.0..180.0
        ) {
            "Invalid current location coordinates."
        }

        require(
            destination.latitude in -90.0..90.0 &&
                    destination.longitude in -180.0..180.0
        ) {
            "Invalid parking destination coordinates."
        }

        // OSRM requires longitude,latitude order.
        val urlString =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${origin.longitude},${origin.latitude};" +
                    "${destination.longitude},${destination.latitude}" +
                    "?overview=full&geometries=geojson"

        val connection = URL(urlString)
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty(
                "User-Agent",
                "SmartParking-Android-App"
            )
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val responseCode = connection.responseCode

            val responseStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw Exception(
                    "OSRM returned HTTP $responseCode without a response."
                )

            if (responseCode !in 200..299) {
                throw Exception(
                    "Routing server error ($responseCode): $response"
                )
            }

            val json = JSONObject(response)

            val status = json.optString("code")

            if (status != "Ok") {
                val message = when (status) {
                    "NoRoute" ->
                        "No driving route exists between your location and this parking."

                    "NoSegment" ->
                        "Your location or parking destination is too far from a mapped road."

                    "InvalidQuery" ->
                        "Invalid coordinates sent to the routing server."

                    else ->
                        json.optString(
                            "message",
                            "Routing failed: $status"
                        )
                }

                throw Exception(message)
            }

            val routes = json.optJSONArray("routes")
                ?: throw Exception("No routes returned by the server.")

            if (routes.length() == 0) {
                throw Exception("No driving route found.")
            }

            val route = routes.getJSONObject(0)

            val coordinates = route
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = buildList {
                for (i in 0 until coordinates.length()) {
                    val coordinate = coordinates.getJSONArray(i)

                    add(
                        LatLng(
                            coordinate.getDouble(1),
                            coordinate.getDouble(0)
                        )
                    )
                }
            }

            if (points.size < 2) {
                throw Exception("The routing server returned an incomplete route.")
            }

            NavigationRoute(
                points = points,
                distanceMeters = route.getDouble("distance"),
                durationSeconds = route.getDouble("duration")
            )

        } finally {
            connection.disconnect()
        }
    }
}