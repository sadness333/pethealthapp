package com.example.prettypetsandfriends.data.entities

import com.example.prettypetsandfriends.R

data class PetProfile(
    val id: String,
    val name: String,
    val age: String,
    val breed: String,
    val weight: String,
    val lastVetVisit: String,
    val vaccinations: List<String>,
    val photoRes: Int
)

