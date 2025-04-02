package com.example.prettypetsandfriends.data.entities.ai

import com.google.gson.annotations.SerializedName

data class ClaudeRequest(
    val model: String,
    val system: String,
    val messages: List<ClaudeMessage>,
    @SerializedName("max_tokens") val maxTokens: Int
)