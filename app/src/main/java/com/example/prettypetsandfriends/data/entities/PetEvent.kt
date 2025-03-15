package com.example.prettypetsandfriends.data.entities

data class PetEvent(
    val id: String = "",
    val petId: String = "",
    val type: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val notificationEnabled: Boolean = false,
    val vetPerson: String = ""
)
