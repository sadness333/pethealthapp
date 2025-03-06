package com.example.prettypetsandfriends.data.entities

import java.util.UUID

data class Pet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val ownerId: String = "",
    val photoUrl: String? = "",
    val statistics: PetStatistics = PetStatistics(),
    val nutrition: PetNutrition = PetNutrition(),
    val vaccinations: List<Vaccination> = emptyList()
) {
    data class Vaccination(
        val id: String = "",
        val petId: String = "",
        val name: String = "",
        val date: Long = 0L,
        val nextDate: Long = 0L,
        val vetPerson: String = ""
    )

    data class PetStatistics(
        val vetVisitsCount: Int = 0,
        val avgHealthScore: Double = 0.0,
        val lastHealthScore: Int = 0,
        val lastWeight: Double = 0.0,
        val avgWeight: Double = 0.0,
        val lastVetVisit: Long = 0,
        val vaccinationsCount: Int = 0,
        val feedingConsistency: Int = 0
    )

    data class PetNutrition(
        val dailyCalories: Int = 0,
        val vetRecommendations: String = ""
    )
}