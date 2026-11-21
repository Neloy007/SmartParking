
package com.example.smartparking.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartparking.presentation.admin.AdminDashboardScreen
import com.example.smartparking.presentation.auth.LoginScreen
import com.example.smartparking.presentation.auth.RegisterScreen
import com.example.smartparking.presentation.driver.DriverHomeScreen
import com.example.smartparking.presentation.driver.DriverVerificationScreen
import com.example.smartparking.presentation.owner.OwnerBookingApprovalScreen
import com.example.smartparking.presentation.owner.OwnerBookingViewModel
import com.example.smartparking.presentation.owner.OwnerHomeScreen
import com.example.smartparking.presentation.owner.OwnerParkingDriversScreen
import com.example.smartparking.presentation.owner.OwnerParkingEditScreen
import com.example.smartparking.presentation.owner.OwnerParkingRegistrationScreen
import com.example.smartparking.presentation.owner.OwnerProfileScreen
import com.example.smartparking.presentation.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier
    ) {

        // SPLASH
        composable(Routes.Splash.route) {
            SplashScreen(navController)
        }

        // LOGIN
        composable(Routes.Login.route) {
            LoginScreen(navController)
        }

        // REGISTER
        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }

        // DRIVER DASHBOARD
        composable(Routes.DriverHome.route) {
            DriverHomeScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // DRIVER VERIFICATION
        composable("driver/verification") {
            DriverVerificationScreen(
                onBack = {
                    navController.popBackStack()
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
                onProfileClick = {
                    navController.navigate(Routes.OwnerProfile.route)
                },
                onBookingRequests = {
                    navController.navigate("owner/booking-approvals")
                },
                onParkingDrivers = {
                    navController.navigate("owner/parking-drivers")
                }
            )
        }

        // OWNER REGISTER PARKING
        composable(Routes.OwnerRegisterParking.route) {
            OwnerParkingRegistrationScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // OWNER EDIT PARKING
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

        // OWNER PROFILE
        composable(Routes.OwnerProfile.route) {
            OwnerProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // OWNER BOOKING APPROVALS
        composable("owner/booking-approvals") {

            val viewModel: OwnerBookingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            OwnerBookingApprovalScreen(
                bookings = uiState.bookings,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                processingBookingId = uiState.processingBookingId,

                onApprove = { booking ->
                    viewModel.approveBooking(booking.id)
                },

                onReject = { booking ->
                    viewModel.rejectBooking(
                        bookingId = booking.id,
                        reason = "Payment could not be verified."
                    )
                },

                onRefresh = {
                    viewModel.refreshBookings()
                }
            )
        }

        // ADMIN DASHBOARD
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // OWNER PARKING DRIVERS
        composable("owner/parking-drivers") {
            OwnerParkingDriversScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
