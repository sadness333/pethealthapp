package com.example.prettypetsandfriends.data.entities

import androidx.compose.ui.text.intl.Locale
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


data class Pet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val birthYear: String = "",
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

    val age: String
        get() {
            return try {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val birthDate = LocalDate.parse(birthYear, formatter)
                val today = LocalDate.now()
                val period = Period.between(birthDate, today)
                val years = period.years
                val months = period.months

                val yearsText = when {
                    years == 0 -> ""
                    years % 10 == 1 && years % 100 != 11 -> "$years год"
                    years % 10 in 2..4 && (years % 100 < 10 || years % 100 >= 20) -> "$years года"
                    else -> "$years лет"
                }

                val monthsText = when {
                    months == 0 -> ""
                    months % 10 == 1 && months % 100 != 11 -> "$months месяц"
                    months % 10 in 2..4 && (months % 100 < 10 || months % 100 >= 20) -> "$months месяца"
                    else -> "$months месяцев"
                }

                listOf(yearsText, monthsText).filter { it.isNotEmpty() }.joinToString(" ")
            } catch (e: Exception) {
                "Некорректная дата рождения"
            }
        }
}