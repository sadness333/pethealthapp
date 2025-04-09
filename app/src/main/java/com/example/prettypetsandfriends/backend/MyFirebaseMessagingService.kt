package com.example.prettypetsandfriends.backend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.prettypetsandfriends.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        val currentUser = Firebase.auth.currentUser
        currentUser?.let { user ->
            val userRef = Firebase.database.reference
                .child("users")
                .child(user.uid)
                .child("fcmToken")

            userRef.get().addOnSuccessListener { snapshot ->
                val existingToken = snapshot.getValue(String::class.java)
                if (existingToken != newToken) {
                    userRef.setValue(newToken)
                        .addOnSuccessListener {
                            Log.d("FCM", "Токен обновлен в базе данных")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Ошибка обновления токена: ${e.message}")
                        }
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!NotificationHelper.areNotificationsEnabled(this)) return

        message.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}