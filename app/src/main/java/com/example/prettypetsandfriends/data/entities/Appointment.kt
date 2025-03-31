package com.example.prettypetsandfriends.data.entities

data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val petId: String = "",
    val date: String = "",
    val ownerId: String = "",
    val time: String = "",
    val reason: String = "",
    val notes: String = "",
    val status: String = "pending",
    val createdAt: String = ""

) {
    constructor() : this("", "", "", "","", "", "", "", "pending", "")
}