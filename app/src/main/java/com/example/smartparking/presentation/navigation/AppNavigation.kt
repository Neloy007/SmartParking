package com.example.smartparking.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartparking.presentation.auth.LoginScreen
import com.example.smartparking.presentation.auth.RegisterScreen
import com.example.smartparking.presentation.driver.DriverHomeScreen
import com.example.smartparking.presentation.owner.OwnerHomeScreen
import com.example.smartparking.presentation.splash.SplashScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {

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
            DriverHomeScreen()
        }

        composable(Routes.OwnerHome.route) {
            OwnerHomeScreen()
        }
    }
}