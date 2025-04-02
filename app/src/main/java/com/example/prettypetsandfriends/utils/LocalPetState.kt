package com.example.prettypetsandfriends.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.prettypetsandfriends.backend.PetState

val LocalPetState = staticCompositionLocalOf<PetState> {
    error("PetState not provided!")
}