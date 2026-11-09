
package com.example.smartparking.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.smartparking.presentation.auth.LoginScreen
import com.example.smartparking.presentation.auth.RegisterScreen
import com.example.smartparking.presentation.driver.DriverHomeScreen
import com.example.smartparking.presentation.owner.OwnerHomeScreen
import com.example.smartparking.presentation.owner.OwnerParkingRegistrationScreen
import com.example.smartparking.presentation.owner.OwnerParkingEditScreen
import com.example.smartparking.presentation.splash.SplashScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier
    ) {

        composable(Routes.Splash.route) {
            SplashScreen(navController)
        }

        composable(Routes.Login.route) {
            LoginScreen(navController)
        }

        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }

        composable(Routes.DriverHome.route) {
            DriverHomeScreen(
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // OWNER DASHBOARD
        composable(Routes.OwnerHome.route) {
            OwnerHomeScreen(
                onRegisterParking = {
                    navController.navigate(Routes.OwnerRegisterParking.route)
                },
                onEditParking = { parking ->
                    navController.navigate("owner/editParking/${parking.id}")
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // REGISTER PARKING
        composable(Routes.OwnerRegisterParking.route) {
            OwnerParkingRegistrationScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // EDIT PARKING
        composable(
            route = "owner/editParking/{parkingId}",
            arguments = listOf(
                navArgument("parkingId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val parkingId = backStackEntry.arguments
                ?.getString("parkingId")
                ?: return@composable

            OwnerParkingEditScreen(
                parkingId = parkingId,
                onBack = {
                    navController.popBackStack()
                },
                onUpdated = {
                    navController.popBackStack()
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }
    }
}
