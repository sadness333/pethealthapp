package com.example.prettypetsandfriends.data.entities

data class FeedingTemplate(
    val id: String = "",
    val foodName: String = "",
    val type: FoodType = FoodType.HOMEMADE,
    val quantity: Float = 0f,
    val calories: Float = 0f,
    val comment: String = "",
    val presentation: String = ""
)