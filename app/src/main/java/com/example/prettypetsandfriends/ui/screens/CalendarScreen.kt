package com.example.prettypetsandfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class CalendarEvent(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val date: LocalDate,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Основной экран с календарём
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь событий") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEventDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить событие")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CalendarGrid(events = events)
        }
    }

    // Диалог добавления события
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { newEvent ->
                events = events + newEvent
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun CalendarGrid(events: List<CalendarEvent>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(events.size) { index ->
            val event = events[index]
            EventCard(event = event, onEditEvent = { editedEvent ->
                // Редактируем событие
            }, onDeleteEvent = { eventId ->
                // Удаляем событие по ID
            })
        }
    }
}

@Composable
fun EventCard(event: CalendarEvent, onEditEvent: (CalendarEvent) -> Unit, onDeleteEvent: (UUID) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEditEvent(event) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = event.color.copy(alpha = 0.1f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onDeleteEvent(event.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
                IconButton(onClick = { onEditEvent(event) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onAddEvent: (CalendarEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить новое событие") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название события") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Простой выбор даты
                Text("Дата события: ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        selectedDate = LocalDate.now().plusDays(1) // Пример, можно заменить на DatePicker
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выбрать дату")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Выбор цвета
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Color(0xFF2196F3),
                        Color(0xFF4CAF50),
                        Color(0xFFFFC107),
                        Color(0xFFFF5722)
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Выбрано",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        onAddEvent(CalendarEvent(title = title, date = selectedDate, color = selectedColor))
                    }
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
