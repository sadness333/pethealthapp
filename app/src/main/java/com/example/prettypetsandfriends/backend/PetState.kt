package com.example.prettypetsandfriends.backend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.backend.repository.PetRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PetState(val petRepository: PetRepository) {
    var selectedPet by mutableStateOf<Pet?>(null)
    var allPets by mutableStateOf<List<Pet>>(emptyList())
    private val auth = Firebase.auth


    fun selectPet(pet: Pet) {
        selectedPet = pet
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            auth.currentUser?.let { user ->
                loadPets(user.uid)
            }
        }
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
                        birthYear = pet.birthYear,
                        ownerId = pet.ownerId,
                        weight = pet.weight,
                        weightHistory = pet.weightHistory,
                        photoUrl = pet.photoUrl,
                        nutrition = pet.nutrition
                    )
                }
                selectedPet = selectedPet ?: allPets.firstOrNull()
            }
        } catch (e: Exception) {
            println("Error loading pets: ${e.message}")
        }
    }

    fun getPetById(id: String): Pet? {
        return allPets.find { it.id == id }
    }
}