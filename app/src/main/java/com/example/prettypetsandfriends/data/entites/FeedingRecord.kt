package com.example.prettypetsandfriends.data.entites

import java.time.LocalDateTime
import java.util.UUID

data class FeedingRecord(
    val id: UUID = UUID.randomUUID(),
    val foodType: String,
    val quantity: String,
    val feedingTime: LocalDateTime,
    val comment: String
)
