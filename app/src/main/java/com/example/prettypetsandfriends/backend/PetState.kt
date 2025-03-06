package com.example.prettypetsandfriends.backend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.repository.PetRepository


class PetState(val petRepository: PetRepository) {
    var selectedPet by mutableStateOf<Pet?>(null)
    var allPets by mutableStateOf<List<Pet>>(emptyList())

    fun selectPet(pet: Pet) {
        selectedPet = pet
    }

    suspend fun loadPets(uid: String) {
        try {
            val petList = petRepository.observeUserPets(uid)
            petList.collect { pets ->
                allPets = pets.map { pet ->
                    Pet(
                        id = pet.id,
                        name = pet.name,
                        type = pet.type,
                        breed = pet.breed,
                        age = pet.age,
                        ownerId = pet.ownerId,
                        photoUrl = pet.photoUrl,
                        statistics = pet.statistics,
                        nutrition = pet.nutrition
                    )
                }
                selectedPet = allPets.firstOrNull()
            }
        } catch (e: Exception) {
            println("Error loading pets: ${e.message}")
        }
    }

    fun getPetById(id: String): Pet? {
        return allPets.find { it.id == id }
    }
}