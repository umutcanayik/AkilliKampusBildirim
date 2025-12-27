package com.tufanpirihan.akillikampusbildirim.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("create_notification") {
            CreateNotificationScreen(navController = navController)
        }
        composable("notification_detail/{notificationJson}") { backStackEntry ->
            val notificationJson = backStackEntry.arguments?.getString("notificationJson") ?: ""
            NotificationDetailScreen(
                navController = navController,
                notificationJson = notificationJson
            )
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("map") {
            MapScreen(navController = navController)
        }
    }
}