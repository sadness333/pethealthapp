package com.example.prettypetsandfriends.data.entities

data class User(
    val uid: String = "",
    val email: String = "",
    val phone: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val pets: Map<String, Boolean> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)