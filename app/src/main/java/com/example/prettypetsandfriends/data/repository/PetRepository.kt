package com.example.prettypetsandfriends.data.repository

import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.entities.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PetRepository {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference

    fun getCurrentUser() = auth.currentUser

    suspend fun addPet(pet: Pet): String = suspendCoroutine { continuation ->
        val newPetRef = database.child("pets").push()
        val petId = newPetRef.key ?: ""

        newPetRef.setValue(pet.copy(id = petId))
            .addOnSuccessListener {
                database.child("users/${pet.ownerId}/pets/$petId").setValue(true)
                continuation.resume(petId)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
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