package com.example.prettypetsandfriends.data.entities

data class WeightHistory(
    val id: String = "",
    val petId: String? = "",
    val value: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)