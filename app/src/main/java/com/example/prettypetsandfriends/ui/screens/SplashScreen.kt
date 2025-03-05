package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val currentUser = Firebase.auth.currentUser

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pet Health Tracker", style = MaterialTheme.typography.headlineLarge)

        LaunchedEffect(Unit) {
            delay(2000)
            if (currentUser != null) {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            } else {
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }
}