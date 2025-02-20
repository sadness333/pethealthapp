package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.entites.CalendarEvent
import com.example.prettypetsandfriends.data.entites.PetsRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID

@Composable
fun CalendarScreen(navController: NavController) {
    var events by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEvent?>(null) }
    var showPetDropdown by remember { mutableStateOf(false) }
    val pets = remember { PetsRepository.pets }


    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                showPetDropdown = showPetDropdown,
                onPetClick = { showPetDropdown = true },
                onDismiss = { showPetDropdown = false },
                pets = pets,
                name = "Календарь",
                )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить событие"
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                MiniCalendar(events = events)
                CalendarEventList(
                    events = events,
                    onEditEvent = { event -> eventToEdit = event },
                    onDeleteEvent = { eventId -> events = events.filter { it.id != eventId } }
                )
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { newEvent -> events = events + newEvent }
        )
    }

    eventToEdit?.let { event ->
        EditEventDialog(
            event = event,
            onDismiss = { eventToEdit = null },
            onEditEvent = { editedEvent ->
                events = events.map { if (it.id == editedEvent.id) editedEvent else it }
                eventToEdit = null
            }
        )
    }
}

@Composable
fun MiniCalendar(events: List<CalendarEvent>) {
    var displayedMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Предыдущий месяц"
                    )
                }

                Text(
                    text = displayedMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru"))
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Следующий месяц"
                    )
                }
            }

            val firstDayOfMonth = displayedMonth
            val firstVisibleDay = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val days = List(42) { firstVisibleDay.plusDays(it.toLong()) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(days) { day ->
                    val isCurrentMonth = day.month == displayedMonth.month
                    val dayEvents = events.filter { it.date == day }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color = if (isCurrentMonth) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable(enabled = isCurrentMonth) { /* Handle day click */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (dayEvents.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    dayEvents.take(3).forEach { event ->
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(event.color)
                                        )
                                    }
                                    if (dayEvents.size > 3) {
                                        Text(
                                            "+${dayEvents.size - 3}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarEventList(
    events: List<CalendarEvent>,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (UUID) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(
                event = event,
                onEditEvent = onEditEvent,
                onDeleteEvent = onDeleteEvent,
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(event.color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.date.format(
                    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledIconButton(
                    onClick = { onEditEvent(event) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = { showDeleteDialog = true },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить событие?") },
            text = { Text("Вы уверены, что хотите безвозвратно удалить это событие?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvent(event.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    EventDialogContent(
        title = title,
        onTitleChange = { title = it },
        selectedColor = selectedColor,
        onColorChange = { selectedColor = it },
        dateState = dateState,
        onConfirm = {
            val date = Instant.ofEpochMilli(dateState.selectedDateMillis!!)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onAddEvent(CalendarEvent(title = title, date = date, color = selectedColor))
            onDismiss()
        },
        onDismiss = onDismiss,
        confirmEnabled = title.isNotBlank()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onEditEvent: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var selectedColor by remember { mutableStateOf(event.color) }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = event.date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    EventDialogContent(
        title = title,
        onTitleChange = { title = it },
        selectedColor = selectedColor,
        onColorChange = { selectedColor = it },
        dateState = dateState,
        onConfirm = {
            val date = Instant.ofEpochMilli(dateState.selectedDateMillis!!)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onEditEvent(event.copy(title = title, date = date, color = selectedColor))
            onDismiss()
        },
        onDismiss = onDismiss,
        confirmEnabled = title.isNotBlank()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDialogContent(
    title: String,
    onTitleChange: (String) -> Unit,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    dateState: DatePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Событие", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Название события") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Дата события:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                DatePicker(
                    state = dateState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp) /* 280dp */
                        .padding( horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Цвет события:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ColorSelectionRow(
                    selectedColor = selectedColor,
                    onColorSelected = onColorChange
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

@Composable
private fun ColorSelectionRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFFF5722),  // Deep Orange
        Color(0xFF9C27B0)   // Purple
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selectedColor) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}