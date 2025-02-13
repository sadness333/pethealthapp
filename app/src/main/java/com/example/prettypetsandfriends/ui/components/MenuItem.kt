package com.example.prettypetsandfriends.ui.components

import androidx.compose.ui.graphics.painter.Painter

data class MenuItem(
    val title: String,
    val icon: Painter,
    val route: String
)