package com.example.prettypetsandfriends.data.entites

import androidx.annotation.DrawableRes
import com.example.prettypetsandfriends.R

enum class FoodType(
    val displayName: String,
    @DrawableRes val iconRes: Int
) {
    DRY("Сухой", R.drawable.ic_weight),
    WET("Влажный", R.drawable.ic_grass),
    HOMEMADE("Домашний", R.drawable.ic_eating)
}
