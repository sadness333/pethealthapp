package com.example.prettypetsandfriends.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prettypetsandfriends.ui.screens.AiChatScreen
import com.example.prettypetsandfriends.ui.screens.AuthScreen
import com.example.prettypetsandfriends.ui.screens.CalendarScreen
import com.example.prettypetsandfriends.ui.screens.FeedingScreen
import com.example.prettypetsandfriends.ui.screens.HealthDiaryScreen
import com.example.prettypetsandfriends.ui.screens.LoginScreen
import com.example.prettypetsandfriends.ui.screens.MainScreen
import com.example.prettypetsandfriends.ui.screens.PetProfileScreen
import com.example.prettypetsandfriends.ui.screens.SplashScreen
import com.example.prettypetsandfriends.ui.screens.UserProfileScreen

@Composable
fun NavigationPetApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("auth") { AuthScreen(navController) }
        composable("health_diary") { HealthDiaryScreen() }
        composable("ai_assistant") { AiChatScreen(navController) }
        composable("calendar") { CalendarScreen(navController) }
        composable("nutrition") { FeedingScreen(navController) }
        composable("profile") { UserProfileScreen(navController) }
        composable(
            route = "pet_profile/{petId}",
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetProfileScreen(petId = petId)
        }
    }
}
