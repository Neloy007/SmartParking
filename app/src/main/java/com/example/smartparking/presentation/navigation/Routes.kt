package com.example.smartparking.presentation.navigation

sealed class Routes(val route: String) {

    data object Splash : Routes("splash")

    data object Login : Routes("login")

    data object Register : Routes("register")

    data object DriverHome : Routes("driver_home")

    data object OwnerHome : Routes("owner_home")

    data object OwnerRegisterParking : Routes("owner_register_parking")


    data object DriverNavigation :
        Routes("driver_navigation/{latitude}/{longitude}/{parkingName}") {

        fun createRoute(
            latitude: Double,
            longitude: Double,
            parkingName: String
        ): String {
            return "driver_navigation/$latitude/$longitude/" +
                    android.net.Uri.encode(parkingName)
        }
    }

}