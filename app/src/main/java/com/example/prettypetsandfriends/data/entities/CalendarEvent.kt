package com.example.prettypetsandfriends.data.entities

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class CalendarEvent(
    val id: UUID = UUID.randomUUID(),
    val title: String = "",
    val type: EventType = EventType.SINGLE,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime? = null,
    val repeatMode: RepeatMode? = null,
    val daysOfWeek: List<DayOfWeek> = emptyList(),
    val notificationEnabled: Boolean = false,
    val color: Color = Color(0xFF4CAF50),
    val petId: String = ""
)