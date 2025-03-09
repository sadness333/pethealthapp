package com.example.prettypetsandfriends.data.repository

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
import kotlinx.coroutines.withContext

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

    suspend fun updateUserProfile(name: String, photoUrl: String?) {
        val uid = getCurrentUserId() ?: return
        val updates = mapOf(
            "name" to name,
            "photoUrl" to photoUrl
        )
        database.child("users/$uid").updateChildren(updates).await()
    }

    fun observeUserPets(): Flow<List<Pet>> = callbackFlow {
        val uid = getCurrentUserId() ?: run {
            close()
            return@callbackFlow
        }

        val listener = database.child("users/$uid/pets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val petIds = snapshot.children.mapNotNull { it.key }
                    val pets = mutableListOf<Pet>()

                    petIds.forEach { petId ->
                        database.child("pets/$petId").get().addOnSuccessListener { petSnapshot ->
                            petSnapshot.getValue(Pet::class.java)?.let {
                                pets.add(it.copy(id = petId))
                                if (pets.size == petIds.size) {
                                    trySend(pets)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    suspend fun getPetDetails(petId: String): Pet? {
        return withContext(Dispatchers.IO) {
            database.child("pets/$petId").get().await()
                .getValue(Pet::class.java)
                ?.copy(id = petId)
        }
    }
    // endregion

    // region Health Data
    fun observeWeightHistory(petId: String): Flow<List<WeightHistory>> = callbackFlow {
        val listener = database.child("weightHistory").orderByChild("petId").equalTo(petId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull {
                        it.key?.let { it1 -> it.getValue(WeightHistory::class.java)?.copy(id = it1) }
                    }
                    trySend(history)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    fun observeVaccinations(petId: String): Flow<List<Pet.Vaccination>> = callbackFlow {
        val listener = database.child("vaccinations").orderByChild("petId").equalTo(petId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val vaccines = snapshot.children.mapNotNull {
                        it.key?.let { it1 -> it.getValue(Pet.Vaccination::class.java)?.copy(id = it1) }
                    }
                    trySend(vaccines)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)
    // endregion

    // region Documents & Events
    fun observeDocuments(petId: String): Flow<List<Document>> = callbackFlow {
        val listener = database.child("documents").orderByChild("petId").equalTo(petId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val docs = snapshot.children.mapNotNull {
                        it.key?.let { it1 -> it.getValue(Document::class.java)?.copy(id = it1) }
                    }
                    trySend(docs)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    fun observeNearestEvent(petState: PetState): Flow<PetEvent?> = callbackFlow {
        val eventsRef = database.child("events")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children.mapNotNull { ds ->
                    ds.getValue(PetEvent::class.java)?.copy(id = ds.key.toString())
                }

                val requiredPetIds = petState.allPets.map { it.id }
                val now = System.currentTimeMillis()

                val validEvents = events.filter { event ->
                    event.petId in requiredPetIds && event.timestamp > now
                }

                val nearestEvent = validEvents.minByOrNull { it.timestamp }

                trySend(nearestEvent)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        eventsRef.addValueEventListener(listener)

        awaitClose {
            eventsRef.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)

}