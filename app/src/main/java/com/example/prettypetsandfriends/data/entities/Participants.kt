package com.example.prettypetsandfriends.data.entities

import java.io.Serializable

data class Participants(
    val client: String = "",
    val doctor: String = ""
): Serializable
