package com.example.prettypetsandfriends.data.repository

import android.util.Log
import com.example.prettypetsandfriends.data.entities.FeedingRecord
import com.example.prettypetsandfriends.data.entities.FoodType
import com.example.prettypetsandfriends.data.entities.NutritionData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FeedingRepository {
    private val database = Firebase.database

    fun getFeedingRecords(petId: String, onUpdate: (List<FeedingRecord>) -> Unit) {
        database.getReference("pets/$petId/nutrition/feedingRecords")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val records = snapshot.children.mapNotNull {
                        try {
                            it.child("nutrition").getValue(NutritionData::class.java)?.let { nutrition ->
                                FeedingRecord(
                                    id = it.key ?: "",
                                    foodName = it.child("foodName").getValue(String::class.java) ?: "",
                                    comment = it.child("comment").getValue(String::class.java) ?: "",
                                    feedingTime = it.child("feedingTime").getValue(String::class.java) ?: "",
                                    petId = petId,
                                    nutrition = it.child("nutrition").getValue(NutritionData::class.java) ?: NutritionData(),
                                    quantity = it.child("quantity").getValue(Float::class.java) ?: 0.0f,
                                    type = it.child("type").getValue(FoodType::class.java) ?: FoodType.HOMEMADE
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("FeedingRepo", "Error parsing record", e)
                            null
                        }
                    }
                    onUpdate(records)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FeedingRepo", "Error fetching data", error.toException())
                }
            })
    }

    fun addFeedingRecord(petId: String, record: FeedingRecord) {
        val recordsRef = database.getReference("pets/$petId/nutrition/feedingRecords")
        val newRef = recordsRef.push()
        val recordWithId = record.copy(id = newRef.key ?: "")
        newRef.setValue(recordWithId)
    }

    fun deleteFeedingRecord(petId: String, recordId: String) {
        database.getReference("pets/$petId/nutrition/feedingRecords/$recordId")
            .removeValue()
            .addOnSuccessListener {
                Log.d("FeedingRepo", "Record $recordId deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FeedingRepo", "Error deleting record", e)
            }
    }


    private fun DataSnapshot.toRecord(): FeedingRecord? {
        return try {
            getValue(FeedingRecord::class.java)?.copy(id = key ?: "")
        } catch (e: Exception) {
            null
        }
    }
}