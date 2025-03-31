package com.example.prettypetsandfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.data.entities.ChatMessage
import com.example.prettypetsandfriends.data.entities.DateMessageGroup
import com.example.prettypetsandfriends.data.entities.Vet
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VetChatScreen(navController: NavController, chatId: String) {
    var vet by remember { mutableStateOf<Vet?>(null) }
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val userId = Firebase.auth.currentUser?.uid ?: ""
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database

    val groupedMessages by remember(messages) {
        derivedStateOf {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            messages
                .groupBy { message ->
                    dateFormat.format(Date(message.getTimestampLong()))
                }
                .map { (date, msgs) -> DateMessageGroup(date, msgs) }
        }
    }

    LaunchedEffect(groupedMessages) {
        if (groupedMessages.isNotEmpty()) {
            val totalItems = groupedMessages.sumOf { 1 + it.messages.size }
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    LaunchedEffect(chatId) {
        database.getReference("chats/$chatId/participants/doctor")
            .get().addOnSuccessListener { snapshot ->
                snapshot.getValue(String::class.java)?.let { doctorId ->
                    database.getReference("doctors/$doctorId")
                        .get().addOnSuccessListener { doctorSnapshot ->
                            vet = doctorSnapshot.getValue(Vet::class.java)
                        }
                }
            }
    }

    LaunchedEffect(chatId) {
        database.getReference("chats/$chatId/messages")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newMessages = snapshot.children.mapNotNull { msgSnapshot ->
                        try {
                            val msg = msgSnapshot.getValue(ChatMessage::class.java)
                            msg?.copy(id = msgSnapshot.key ?: "")
                        } catch (e: Exception) {
                            Log.e("VetChat", "Error parsing message", e)
                            null
                        }
                    }.sortedBy { it.getTimestampLong() }

                    messages.clear()
                    messages.addAll(newMessages)

                    coroutineScope.launch {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("VetChat", "Messages loading cancelled", error.toException())
                }
            })
    }

    Scaffold(
        topBar = { CustomTopBar(
            navController = navController,
            name = "Диалог",
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )},
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                groupedMessages.forEach { group ->
                    item(key = "header_${group.dateString}") {
                        DateHeader(date = group.dateString)
                    }
                    items(group.messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isUserMessage = message.senderId == userId,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            ChatInputField(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        val messageRef = database.getReference("chats/$chatId/messages").push()
                        val newMessage = ChatMessage(
                            id = messageRef.key ?: "",
                            text = messageText,
                            senderId = userId,
                            timestamp = ServerValue.TIMESTAMP
                        )
                        messageRef.setValue(newMessage)
                        messageText = ""
                    }
                }
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isUserMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isUserMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor,
                contentColor = textColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUserMessage) 4.dp else 16.dp,
                bottomEnd = if (isUserMessage) 16.dp else 4.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.getTimestampLong())),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ChatInputField(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Введите сообщение...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )

            IconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank(),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

