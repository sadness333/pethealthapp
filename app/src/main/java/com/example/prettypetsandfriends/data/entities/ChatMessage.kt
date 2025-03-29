package com.example.prettypetsandfriends.data.entities

import com.google.firebase.database.ServerValue
import java.util.UUID

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val isUser: Boolean = false,
    val timestamp: Any = ServerValue.TIMESTAMP
) {

    fun getTimestampLong(): Long {
        return when (timestamp) {
            is Long -> timestamp
            is Double -> timestamp.toLong()
            is Map<*, *> -> (timestamp["timestamp"] as? Long) ?: 0L
            else -> 0L
        }
    }
}