package com.example.prettypetsandfriends.ui.screens

import android.graphics.ColorSpace.Rgb
import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.data.entities.Chat
import com.example.prettypetsandfriends.data.entities.ChatMessage
import com.example.prettypetsandfriends.data.entities.Participants
import com.example.prettypetsandfriends.data.entities.Vet
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(navController: NavController) {
    val chats = remember { mutableStateListOf<Chat>() }
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    var loading by remember { mutableStateOf(true) }
    var showVetDialog by remember { mutableStateOf(false) }
    var showPetDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        Firebase.database.getReference("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempChats = mutableListOf<Chat>()
                    snapshot.children.forEach { chatSnapshot ->
                        val participants = chatSnapshot.child("participants")
                            .getValue(Participants::class.java)

                        if (participants?.client == currentUserId) {
                            val messages = chatSnapshot.child("messages")
                                .children
                                .mapNotNull {
                                    it.getValue(ChatMessage::class.java)?.copy(id = it.key ?: "")
                                }
                                .sortedBy { it.getTimestampLong() }

                            val lastMessage = messages.lastOrNull()?.text ?: "Нет сообщений"

                            tempChats.add(
                                Chat(
                                    id = chatSnapshot.key ?: "",
                                    participants = participants,
                                    messages = messages.associateBy { it.id },
                                    lastMessage = lastMessage,
                                    timestamp = messages.lastOrNull()?.getTimestampLong() ?: 0
                                )
                            )
                        }
                    }

                    chats.clear()
                    chats.addAll(
                        tempChats.sortedByDescending { it.timestamp }
                    )
                    loading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    loading = false
                    Log.e("ChatList", "Database Error: ${error.toException()}")
                }
            })
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Чаты",
                showPetDropdown = showPetDropdown,
                onPetClick = { showPetDropdown = true },
                onDismiss = { showPetDropdown = false },

            )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showVetDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новый чат"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 8.dp)
                .fillMaxSize()
        ) {
            ChatListItem(
                name = "ИИ Помощник",
                lastMessage = "Задайте вопрос о здоровье питомца",
                photoUrl = "https://cdn-icons-png.flaticon.com/128/4616/4616790.png",
                modifier = Modifier.padding(bottom = 4.dp),
                onClick = { navController.navigate("ai_chat") }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                if (chats.isEmpty()) {
                    EmptyChatList()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        items(chats) { chat ->
                            val vetId = chat.participants.doctor
                            var vet by remember { mutableStateOf<Vet?>(null) }

                            LaunchedEffect(vetId) {
                                Firebase.database.getReference("doctors/$vetId")
                                    .get().addOnSuccessListener {
                                        vet = it.getValue(Vet::class.java)
                                    }
                            }

                            vet?.let {
                                ChatListItem(
                                    name = it.name,
                                    lastMessage = chat.lastMessage,
                                    photoUrl = it.photo,
                                    timestamp = chat.timestamp,
                                    onClick = { navController.navigate("vet_chat/${chat.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showVetDialog) {
            SelectVetDialog(
                currentUserId = currentUserId,
                existingChats = chats,
                onDismiss = { showVetDialog = false },
                onVetSelected = { vetId ->
                    val newChatRef = Firebase.database.getReference("chats").push()
                    val participants = Participants(
                        client = currentUserId,
                        doctor = vetId
                    )

                    newChatRef.setValue(
                        mapOf(
                            "participants" to participants,
                            "messages" to mapOf<String, Any>()
                        )
                    ).addOnSuccessListener {
                        navController.navigate("vet_chat/${newChatRef.key}")
                        showVetDialog = false
                    }
                }
            )
        }

    }
}

@Composable
fun ChatListItem(
    name: String,
    lastMessage: String,
    photoUrl: String,
    modifier: Modifier = Modifier,
    timestamp: Long = 0,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = when {
                            timestamp == 0L -> ""
                            isToday(timestamp) -> formatTime(timestamp)
                            else -> formatDate(timestamp)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun EmptyChatList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Forum,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Нет активных чатов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Нажмите на кнопку ниже, чтобы начать новый диалог с ветеринаром",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun SelectVetDialog(
    currentUserId: String,
    existingChats: List<Chat>,
    onDismiss: () -> Unit,
    onVetSelected: (String) -> Unit
) {
    val vets = remember { mutableStateListOf<Vet>() }
    var loading by remember { mutableStateOf(true) }

    val existingVetIds = existingChats.map { it.participants.doctor }.toSet()

    LaunchedEffect(Unit) {
        Firebase.database.getReference("doctors")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vets.clear()
                    snapshot.children.forEach { vetSnapshot ->
                        vetSnapshot.getValue(Vet::class.java)?.let { vet ->
                            if (!existingVetIds.contains(vet.id)) {
                                vets.add(vet)
                            }
                        }
                    }
                    loading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    loading = false
                    Log.e("SelectVetDialog", "Error: ${error.message}")
                }
            })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(vertical = 24.dp),
        title = {
            Text(
                text = "Выберите ветеринара",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            when {
                loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                vets.isEmpty() -> Text(
                    text = "Все доступные ветеринары уже выбраны",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(vets) { vet ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) { onVetSelected(vet.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    AsyncImage(
                                        model = vet.photo,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = vet.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = vet.specialization,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 88.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            ) {
                Text("ОТМЕНА", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}