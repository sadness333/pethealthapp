package com.example.prettypetsandfriends.data.entities
import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class HealthRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val title: String,
    val description: String,
    val type: RecordType,
    val photos: List<Int> = emptyList()
)

enum class RecordType {
    WEIGHT, VACCINATION, SYMPTOM, MEDICATION, OTHER
}