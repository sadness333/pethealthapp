package com.example.prettypetsandfriends.backend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entities.PetProfile

class PetState {
    var selectedPet by mutableStateOf<PetProfile?>(null)
    var allPets by mutableStateOf<List<PetProfile>>(emptyList())

    fun selectPet(pet: PetProfile) {
        selectedPet = pet
    }

    fun addPet(pet: PetProfile) {
        allPets = allPets + pet
    }

    fun loadPets() {
        allPets = listOf(
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
                photoRes = R.drawable.ic_grass
            )
        )
        selectedPet = allPets.firstOrNull()
    }

    fun getPetById(id: String): PetProfile? {
        return allPets.find { it.id == id }
    }
}