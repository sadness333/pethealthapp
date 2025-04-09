package com.example.prettypetsandfriends.ui.components

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prettypetsandfriends.backend.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit,
    context: android.content.Context
) {
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationsEnabled = NotificationHelper.areNotificationsEnabled(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка уведомлений") },
        text = {
            Column {
                Text(
                    "Управляйте получением внутренних уведомлений о важных событиях",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(
                        "Включить уведомления",
                        modifier = Modifier.weight(1f) )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isEnabled ->
                            notificationsEnabled = isEnabled
                            NotificationHelper.setNotificationsEnabled(context, isEnabled)

                            val currentUser = Firebase.auth.currentUser
                            currentUser?.let { user ->
                                val fcmRef = Firebase.database.reference
                                    .child("users")
                                    .child(user.uid)
                                    .child("fcmToken")

                                if (isEnabled) {
                                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                        fcmRef.setValue(token)
                                    }
                                } else {
                                    fcmRef.removeValue()
                                }
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}