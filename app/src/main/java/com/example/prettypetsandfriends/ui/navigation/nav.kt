package com.example.prettypetsandfriends.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prettypetsandfriends.ui.screens.AuthScreen
import com.example.prettypetsandfriends.ui.screens.LoginScreen
import com.example.prettypetsandfriends.ui.screens.MainScreen
import com.example.prettypetsandfriends.ui.screens.SplashScreen

@Composable
fun NavigationPetApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen() }
        composable("auth") { AuthScreen(navController) }
    }
}
