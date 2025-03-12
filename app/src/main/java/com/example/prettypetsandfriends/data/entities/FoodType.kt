package com.example.prettypetsandfriends.data.entities

import com.example.prettypetsandfriends.R

enum class FoodType(
    val displayName: String,
    val iconRes: Int
) {
    DRY("Сухой", R.drawable.ic_dry),
    WET("Влажный", R.drawable.ic_water),
    HOMEMADE("Домашний", R.drawable.ic_eating)
}
