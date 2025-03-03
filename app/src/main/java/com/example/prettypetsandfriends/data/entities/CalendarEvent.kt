package com.example.prettypetsandfriends.data.entities

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.UUID

data class CalendarEvent(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val date: LocalDate,
    val color: Color
)
