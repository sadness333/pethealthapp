package com.example.prettypetsandfriends.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.backend.PetState
import com.example.prettypetsandfriends.ui.navigation.NavigationPetApp
import com.example.prettypetsandfriends.ui.theme.PrettypetsandfriendsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            var currentTheme by remember { mutableStateOf(AppTheme.SYSTEM) }
            val scope = rememberCoroutineScope()
            val petState = remember { PetState().apply { loadPets() } }


            LaunchedEffect(Unit) {
                ThemeManager.getThemeFlow(context).collect { theme ->
                    currentTheme = theme
                }
            }

            PrettypetsandfriendsTheme(
                darkTheme = when (currentTheme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalPetState provides petState) {
                        NavigationPetApp()
                    }
                }
            }
        }
    }
}