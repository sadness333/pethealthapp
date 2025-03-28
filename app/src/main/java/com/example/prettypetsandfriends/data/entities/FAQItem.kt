package com.example.prettypetsandfriends.data.entities

import androidx.compose.ui.graphics.vector.ImageVector

data class FAQItem(
    val question: String,
    val answer: String,
    val icon: ImageVector? = null,
    var expanded: Boolean = false
)