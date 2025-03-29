package com.example.prettypetsandfriends.data.entities

import com.google.firebase.database.ServerValue

data class Chat(
    val id: String = "",
    val participants: Participants = Participants(),
    val messages: Map<String?, ChatMessage?> = emptyMap(),
    val lastMessage: String = "",
    val timestamp: Long = 0
)