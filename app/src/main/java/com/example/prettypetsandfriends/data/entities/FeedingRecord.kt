package com.example.prettypetsandfriends.data.entities

import java.time.LocalDateTime
import java.util.UUID

data class FeedingRecord(
    val id: UUID = UUID.randomUUID(),
    val foodName: String,
    val type: FoodType,
    val nutrition: NutritionData,
    val quantity: Float,
    val feedingTime: LocalDateTime,
    val comment: String
)
