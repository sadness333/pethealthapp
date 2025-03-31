package com.example.prettypetsandfriends.data.entities

data class Vet(
    val id: String = "",
    val name: String = "",
    val specialization: String = "",
    val photo: String = "",
    val base_schedule: BaseSchedule = BaseSchedule(),
    val working_days: List<String> = emptyList(),
    val schedule: Map<String, Map<String, AppointmentSlot>> = emptyMap()
) {
    data class BaseSchedule(
        val start: String = "09:00",
        val end: String = "17:00",
        val slotDuration: Int = 30
    )

    data class AppointmentSlot(
        val appointmentId: String = "",
        val status: String = "pending"
    )
}