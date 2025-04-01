package com.example.prettypetsandfriends.data.entities


data class Pet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val ownerId: String = "",
    val photoUrl: String? = "",
    val nutrition: PetNutrition = PetNutrition(),
    val weightHistory: Map<String, WeightHistory> = emptyMap()
) {

    data class PetNutrition(
        val dailyCalories: Int = 0,
        val vetRecommendations: String = ""
    )
}