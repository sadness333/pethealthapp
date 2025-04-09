package com.example.prettypetsandfriends.data.entities

data class User(
    val uid: String = "",
    val email: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val name: String = "Гость",
    val photoUrl: String = "",
    val bio: String = "",
    val pets: Map<String, Boolean> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val role: String = "user"

)