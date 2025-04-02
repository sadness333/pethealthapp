package com.example.prettypetsandfriends.data.entities

import android.util.Log
import java.util.Calendar


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
        get() = calculateAge()

    private fun calculateAge(): String {
        return try {
            val parts = birthYear.split(".")
            if (parts.size != 3) return "Некорректный формат"

            val year = parts[2].toInt()
            val month = parts[1].toInt()
            val day = parts[0].toInt()
            val today = Calendar.getInstance()
            val birthDate = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }
            var years = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
            var months = today.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
            var days = today.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)

            if (days < 0) {
                months--
                days += today.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years--
                months += 12
            }
            formatAge(years, months)
        } catch (e: Exception) {
            "Ошибка в дате"
        }
    }

    private fun formatAge(years: Int, months: Int): String {
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

        return listOf(yearsText, monthsText)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }

}