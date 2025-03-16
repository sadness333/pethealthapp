package com.example.prettypetsandfriends.data.entities

data class FeedingTemplate(
    val foodName: String,
    val type: FoodType,
    val quantity: Float,
    val calories: Float,
    val comment: String = ""
)
