
package com.example.smartparking.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartparking.presentation.admin.AdminDashboardScreen
import com.example.smartparking.presentation.auth.LoginScreen
import com.example.smartparking.presentation.auth.RegisterScreen
import com.example.smartparking.presentation.driver.DriverHomeScreen
import com.example.smartparking.presentation.owner.OwnerHomeScreen
import com.example.smartparking.presentation.owner.OwnerParkingRegistrationScreen
import com.example.smartparking.presentation.owner.OwnerParkingEditScreen
import com.example.smartparking.presentation.owner.OwnerProfileScreen
import com.example.smartparking.presentation.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

private val ParkwayBlack = Color(0xFF080808)
private val ParkwayLime = Color(0xFFB8F34A)
private val ParkwayWhite = Color(0xFFFFFFFF)
private val ParkwayGray = Color(0xFF777777)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

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
                        popUpTo(Routes.AdminDashboard.route) {
                            inclusive = true
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
                    navController.navigate(
                        Routes.OwnerRegisterParking.route
                    )
                },
                onEditParking = { parking ->
                    navController.navigate(
                        "owner/editParking/${parking.id}"
                    )
                },
                onProfileClick = {
                    navController.navigate(
                        Routes.OwnerProfile.route
                    )
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
                        popUpTo(Routes.OwnerProfile.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ADMIN DASHBOARD - DEMO ONLY
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.AdminDashboard.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDemoDashboardScreen(
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = ParkwayWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Parkway Admin",
                            color = ParkwayBlack,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "DEMO ADMIN PANEL",
                            color = ParkwayGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = ParkwayBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParkwayWhite
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ParkwayWhite)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(25.dp))

            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = ParkwayBlack,
                modifier = Modifier.size(65.dp)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Welcome, Administrator",
                color = ParkwayBlack,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "You entered the Parkway Admin demonstration panel.",
                color = ParkwayGray
            )

            Spacer(Modifier.height(25.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ParkwayLime.copy(alpha = 0.25f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "DEMO MODE",
                        color = ParkwayBlack,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "This screen is not connected to protected administrator data. " +
                                "The PIN does not authenticate you with Firebase.",
                        color = ParkwayBlack,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "Admin modules will be added here:",
                modifier = Modifier.fillMaxWidth(),
                color = ParkwayBlack,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            AdminModuleItem("Owner List")
            AdminModuleItem("Driver List")
            AdminModuleItem("User Profiles")
            AdminModuleItem("Facility Approvals")
            AdminModuleItem("Income & Expenses")

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkwayBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = ParkwayLime
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Logout",
                    color = ParkwayWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdminModuleItem(
    title: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF4F4F4)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = ParkwayBlack
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                color = ParkwayBlack,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Coming soon",
                color = ParkwayGray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
