package com.example.prettypetsandfriends.backend.repository

import androidx.compose.runtime.rememberCoroutineScope
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.entities.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PetRepository {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference

    fun getCurrentUser() = auth.currentUser

    suspend fun deleteAllPets(uid: String) {
        database.child("pets")
            .orderByChild("ownerId")
            .equalTo(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { it.ref.removeValue() }
            }
            .await()
    }

    suspend fun addPet(pet: Pet): String {
        val petId = withContext(Dispatchers.IO) {
            val newPetRef = database.child("pets").push()
            val petId = newPetRef.key ?: ""


            try {
                newPetRef.setValue(pet.copy(id = petId)).await()
                database.child("users/${pet.ownerId}/pets/$petId").setValue(true).await()
                petId
            } catch (e: Exception) {
                throw e
            }
        }
        val weightRep = WeightRepository()

        weightRep.addWeight(
            weight = pet.weight,
            petId = petId,
            notes = "Начальный вес"
        )

        return petId
    }


    fun observeUserPets(uid: String): Flow<List<Pet>> = callbackFlow {
        val listener = database.child("pets")
            .orderByChild("ownerId")
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pets = snapshot.children.mapNotNull { it.getValue(Pet::class.java) }
                    trySend(pets).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }
}