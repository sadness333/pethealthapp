package com.example.prettypetsandfriends.data.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.prettypetsandfriends.data.entities.CalendarEvent
import com.example.prettypetsandfriends.data.entities.EventType
import com.example.prettypetsandfriends.data.entities.RepeatMode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

object CalendarRepository {
    private val database = Firebase.database

    private fun getEventsRef(petId: String) =
        database.getReference("pets/$petId/events")

    fun getEvents(petId: String, onUpdate: (List<CalendarEvent>) -> Unit) {
        getEventsRef(petId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children.mapNotNull { it.toEvent() }
                onUpdate(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarRepo", "Error loading events", error.toException())
            }
        })
    }

    fun addEvent(petId: String, event: CalendarEvent) {
        val eventData = hashMapOf(
            "id" to event.id.toString(),
            "title" to event.title,
            "type" to event.type.name,
            "date" to event.date.toString(),
            "time" to event.time?.toString(),
            "repeatMode" to event.repeatMode?.name,
            "daysOfWeek" to event.daysOfWeek.map { it.name },
            "notificationEnabled" to event.notificationEnabled,
            "color" to event.color.value.toLong(),
            "petId" to event.petId
        )
        getEventsRef(petId).child(event.id.toString()).setValue(eventData)
    }

    fun updateEvent(petId: String, event: CalendarEvent) {
        addEvent(petId, event)
    }

    fun deleteEvent(petId: String, eventId: UUID) {
        getEventsRef(petId).child(eventId.toString()).removeValue()
    }

    private fun DataSnapshot.toEvent(): CalendarEvent? {
        return try {
            CalendarEvent(
                id = UUID.fromString(child("id").getValue<String>()),
                title = (child("title").getValue() ?: "").toString(),
                type = enumValueOf((child("type").getValue() ?: "SINGLE").toString()),
                date = LocalDate.parse(child("date").getValue().toString()),
                time = child("time").getValue<String?>()?.let { LocalTime.parse(it) },
                repeatMode = safeEnumValue<RepeatMode>(child("repeatMode").getValue().toString()),
                daysOfWeek = (child("daysOfWeek").getValue<List<String>>() ?: emptyList())
                    .mapNotNull { safeEnumValue<DayOfWeek>(it) },
                notificationEnabled = child("notificationEnabled").getValue(Boolean::class.java) ?: false,
                color = Color(child("color").getValue<Long>()?.toInt() ?: 0xFF2196F3.toInt()),
                petId = (child("petId").getValue() ?: "").toString()
            )
        } catch (e: Exception) {
            Log.e("CalendarRepo", "Error parsing event", e)
            null
        }
    }

    private inline fun <reified T : Enum<T>> safeEnumValue(value: String?): T? {
        return try {
            enumValueOf<T>(value ?: return null)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun Color.toLong() = (red * 255).toInt().shl(16) or
            (green * 255).toInt().shl(8) or
            (blue * 255).toInt()
}