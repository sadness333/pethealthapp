package com.example.prettypetsandfriends.data.repository

import com.example.prettypetsandfriends.data.entities.HealthRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HealthRecordsRepository {
    private val database = FirebaseDatabase.getInstance().reference

    fun getHealthRecordsFlow(petId: String?): Flow<List<HealthRecord>> = callbackFlow {
        if (petId.isNullOrEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val healthRecordsRef = database.child("pets")
            .child(petId)
            .child("HealthRecord")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = snapshot.children.mapNotNull {
                    it.getValue(HealthRecord::class.java)?.copy(id = it.key ?: "")
                }
                trySend(records)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        healthRecordsRef.addValueEventListener(listener)
        awaitClose { healthRecordsRef.removeEventListener(listener) }
    }
}