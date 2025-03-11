package com.example.prettypetsandfriends.data.repository

import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.data.entities.WeightEntry
import com.example.prettypetsandfriends.data.entities.WeightHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId

class WeightRepository {
    private val database = Firebase.database.reference

    fun getWeightHistory(petId: String?): Flow<List<WeightHistory>> = callbackFlow {
        val weightNode = database.child("pets").child(petId.toString()).child("weightHistory")
        val query = weightNode.orderByChild("petId").equalTo(petId)
        val listener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = snapshot.children.mapNotNull { ds ->
                    ds.getValue(WeightHistory::class.java)
                }.sortedByDescending { it.date }
                trySend(historyList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addWeight(weight: Double, petId: String?, notes: String = "") {
        val weightNode = database.child("pets").child(petId.toString()).child("weightHistory")
        try {
            val key = weightNode.push().key ?: throw Exception("Ошибка генерации ключа")
            val entry = WeightHistory(
                id = key,
                petId = petId,
                value = weight,
                date = System.currentTimeMillis(),
                notes = notes
            )
            weightNode.child(key).setValue(entry).await()
        } catch (e: Exception) {
            throw Exception("Ошибка сохранения: ${e.message}")
        }
    }
}
