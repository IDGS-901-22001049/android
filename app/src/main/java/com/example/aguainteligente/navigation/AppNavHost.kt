package com.example.aguainteligente.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aguainteligente.ui.dashboard.DashboardScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.aguainteligente.ui.auth.LoginScreen
import com.example.aguainteligente.ui.auth.SignUpScreen
import com.example.aguainteligente.ui.dashboard.ConsejosScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Consejos : Screen("consejos")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = if (Firebase.auth.currentUser != null) Screen.Dashboard.route else Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToTips = {
                    navController.navigate(Screen.Consejos.route)
                }
            )
        }
        composable(Screen.Consejos.route) {
            ConsejosScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}