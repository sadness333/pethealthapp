package com.example.prettypetsandfriends.backend

import androidx.compose.runtime.staticCompositionLocalOf

val LocalPetState = staticCompositionLocalOf<PetState> {
    error("PetState not provided!")
}