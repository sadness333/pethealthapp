package com.example.prettypetsandfriends.backend

import android.content.Context
import androidx.core.content.edit

object NotificationHelper {
    private const val PREFS_NAME = "notifications_settings"
    private const val KEY_NOTIFICATIONS = "enable_notifications"

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_NOTIFICATIONS, enabled)
            apply()
        }
    }
}