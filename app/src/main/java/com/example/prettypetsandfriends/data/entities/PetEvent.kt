package com.example.prettypetsandfriends.data.entities

data class PetEvent(
    val id: String = "",
    val petId: String = "",
    val type: String = "",
    val timestamp: Long = 0L,
    val vetPerson: String = ""
)