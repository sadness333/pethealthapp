package com.example.prettypetsandfriends.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.entities.ChatMessage
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiChatScreen(navController: NavController) {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "ИИ-Помощник",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(chatMessages) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        ChatBubble(
                            message = message,
                            isUserMessage = message.isUser
                        )
                    }
                }

                item {
                    if (isLoading) {
                        ThinkingIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            ChatInputField(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        chatMessages.add(
                            ChatMessage(
                                text = messageText,
                                isUser = true,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        messageText = ""
                        isLoading = true

                        CoroutineScope(Dispatchers.IO).launch {
                            delay(1000)
                            withContext(Dispatchers.Main) {
                                chatMessages.add(
                                    ChatMessage(
                                        text = generateAiResponse(),
                                        isUser = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                isLoading = false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ThinkingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DotAnimation()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "ИИ-помощник печатает...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DotAnimation() {
    val dotSize = 12.dp
    val infiniteTransition = rememberInfiniteTransition()

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 0
                1.0f at 300
                0.0f at 600
                0.0f at 1200
            },
            repeatMode = RepeatMode.Restart
        )
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 200
                1.0f at 500
                0.0f at 800
                0.0f at 1200
            },
            repeatMode = RepeatMode.Restart
        )
    )

    val floatAnim3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 400
                1.0f at 700
                0.0f at 1000
                0.0f at 1200
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Row {
        AnimatedDot(floatAnim1, dotSize)
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedDot(floatAnim2, dotSize)
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedDot(floatAnim3, dotSize)
    }
}

@Composable
private fun AnimatedDot(alpha: Float, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

private fun generateAiResponse(): String {
    val responses = listOf(
        "Рекомендую провести общий анализ крови.",
        "Нормальная температура для кошек: 38-39.5°C",
        "Пожалуйста, опишите симптомы подробнее.",
        "Советую записаться на профилактический осмотр.",
        "Ваш питомец должен пить достаточное количество воды.",
        "Рекомендованная частота кормления: 2 раза в день."
    )
    return responses.random()
}