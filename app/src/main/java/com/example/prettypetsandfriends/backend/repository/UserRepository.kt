package com.example.prettypetsandfriends.backend.repository

import android.util.Log
import com.example.prettypetsandfriends.backend.PetState
import com.example.prettypetsandfriends.data.entities.Document
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.entities.PetEvent
import com.example.prettypetsandfriends.data.entities.User
import com.example.prettypetsandfriends.data.entities.WeightHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        bio: String,
        photoUrl: String?
    ) {
        val uid = getCurrentUserId() ?: return
        val updates = mapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "bio" to bio,
            "photoUrl" to photoUrl
        )
        database.child("users/$uid").updateChildren(updates).await()
    }

    suspend fun deleteUserData(uid: String) {
        database.child("users/$uid").removeValue().await()
    }

    fun observeUserData(): Flow<User> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = database.child("users").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)?.copy(uid = uid)
                    user?.let { trySend(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    fun observeNearestEvent(petState: PetState): Flow<PetEvent?> = callbackFlow {
        val petsRef = database.child("pets")
        val dateTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allEvents = snapshot.children.flatMap { petSnapshot ->
                    val eventsSnapshot = petSnapshot.child("events")
                    eventsSnapshot.children.mapNotNull { eventSnapshot ->
                        eventSnapshot.getValue(PetEvent::class.java)
                            ?.copy(id = eventSnapshot.key.toString())
                    }
                }
                val requiredPetIds = petState.allPets.map { it.id }
                val now = System.currentTimeMillis()

                val validEvents = allEvents.filter { event ->
                    val eventTime = try {
                        dateTimeFormat.parse("${event.date} ${event.time}")?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                    event.petId in requiredPetIds && eventTime > now
                }
                val nearestEvent = validEvents.minByOrNull { event ->
                    try {
                        dateTimeFormat.parse("${event.date} ${event.time}")?.time ?: Long.MAX_VALUE
                    } catch (e: Exception) {
                        Long.MAX_VALUE
                    }
                }

                trySend(nearestEvent)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        petsRef.addValueEventListener(listener)

        awaitClose {
            petsRef.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)
}