package com.example.prettypetsandfriends.data.entities

import com.google.firebase.database.PropertyName

data class HealthRecord(
    val id: String = "",
    val date: String = "",
    val description: String = "",
    @PropertyName("petId")
    val petId: String = "",
    val title: String = "",
    val type: String = "",
    @PropertyName("photoUrls")
    val photoUrls: List<String> = emptyList()
)