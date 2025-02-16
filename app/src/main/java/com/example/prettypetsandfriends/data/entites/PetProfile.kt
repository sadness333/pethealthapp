package com.example.prettypetsandfriends.data.entites

import com.example.prettypetsandfriends.R

data class PetProfile(
    val id: String,
    val name: String,
    val age: String,
    val breed: String,
    val weight: String,
    val lastVetVisit: String,
    val vaccinations: List<String>,
    val photoRes: Int
)

object PetsRepository {
    val pets = listOf(
        PetProfile(
            id = "1",
            name = "Morphy",
            age = "2 года",
            breed = "Британская короткошерстная",
            weight = "5.2 кг",
            lastVetVisit = "15.10.2023",
            vaccinations = listOf("Бешенство", "Панлейкопения"),
            photoRes = R.drawable.ic_pets_black
        ),
        PetProfile(
            id = "2",
            name = "Kitten",
            age = "3 года",
            breed = "Мейн-кун",
            weight = "6.0 кг",
            lastVetVisit = "10.10.2023",
            vaccinations = listOf("Бешенство", "Калицивироз"),
            photoRes = R.drawable.ic_pets_black
        )
    )

    fun getPetById(id: String): PetProfile? {
        return pets.find { it.id == id }
    }
}