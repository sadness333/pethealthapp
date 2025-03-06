package com.example.prettypetsandfriends.backend

fun getYearsText(age: Int): String {
    val lastDigit = age % 10
    val lastTwoDigits = age % 100

    return when {
        lastTwoDigits in 11..19 -> "$age лет"
        lastDigit == 1 -> "$age год"
        lastDigit in 2..4 -> "$age года"
        else -> "$age лет"
    }
}