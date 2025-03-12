package com.example.prettypetsandfriends.data.entities

data class FeedingRecord(
    val id: String = "",
    val foodName: String,
    val type: FoodType,
    val nutrition: NutritionData,
    val quantity: Float,
    val feedingTime: String,
    val comment: String = "",
    val petId: String,
)
