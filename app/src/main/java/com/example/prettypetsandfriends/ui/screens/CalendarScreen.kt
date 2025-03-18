package com.example.prettypetsandfriends.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.data.entities.CalendarEvent
import com.example.prettypetsandfriends.data.entities.EventType
import com.example.prettypetsandfriends.data.entities.RepeatMode
import com.example.prettypetsandfriends.data.repository.CalendarRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID

@Composable
fun CalendarScreen(navController: NavController) {
    val context = LocalContext.current
    val petId = LocalPetState.current.selectedPet?.id ?: ""
    var events by remember { mutableStateOf(emptyList<CalendarEvent>()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEvent?>(null) }

    LaunchedEffect(petId) {
        CalendarRepository.getEvents(petId) { events = it }
    }

    Scaffold(
        topBar = {
            CustomTopBar(navController = navController, name = "Календарь")
        },
        bottomBar = { CustomBottomNavigation(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (petId.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Невозможно добавить событие для несуществующего питомца",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showAddEventDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить событие")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 500.dp, max = 1000.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    MiniCalendar(
                        events = events,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DoctorAppointmentCard(
                    events = events,
                    petId = petId,
                    modifier = Modifier.fillMaxWidth()
                ) { updatedEvent, isBooking ->
                    if (isBooking) {
                        CalendarRepository.addEvent(petId, updatedEvent)
                        events = events + updatedEvent
                    } else {
                        CalendarRepository.deleteEvent(petId, updatedEvent.id)
                        events = events.filter { it.id != updatedEvent.id }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "События",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                events.filter { !it.title.startsWith("Запись к врачу:") }.forEach { event ->
                    EventCard(
                        event = event,
                        onEdit = { eventToEdit = it },
                        onDelete = { eventId ->
                            CalendarRepository.deleteEvent(petId, eventId)
                            events = events.filter { it.id != eventId }
                        },
                        petId = petId,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            petId = petId,
            onDismiss = { showAddEventDialog = false },
            onAddEvent = { newEvent ->
                CalendarRepository.addEvent(petId, newEvent)
                events = events + newEvent
            }
        )
    }

    eventToEdit?.let { event ->
        EditEventDialog(
            event = event,
            onDismiss = { eventToEdit = null },
            onEditEvent = { editedEvent ->
                CalendarRepository.updateEvent(petId, editedEvent)
                events = events.map { if (it.id == editedEvent.id) editedEvent else it }
                eventToEdit = null
            }
        )
    }
}

@Composable
fun MiniCalendar(events: List<CalendarEvent>, modifier: Modifier = Modifier) {
    var displayedMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    Card{
    Column(modifier = modifier.pointerInput(Unit) {
        var cumulativeDrag = 0f
        detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
                cumulativeDrag += dragAmount
            },
            onDragEnd = {
                if (cumulativeDrag < -100f) {
                    displayedMonth = displayedMonth.plusMonths(1)
                } else if (cumulativeDrag > 100f) {
                    displayedMonth = displayedMonth.minusMonths(1)
                }
                cumulativeDrag = 0f
            }
        )
    })

    {
        CalendarHeader(displayedMonth) { displayedMonth = it }
        Spacer(modifier = Modifier.height(16.dp))
        CalendarGrid(displayedMonth, events)
    }
    }
}

@Composable
private fun CalendarHeader(
    displayedMonth: LocalDate,
    onMonthChanged: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChanged(displayedMonth.minusMonths(1)) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
        }

        Text(
            text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru"))),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(
            onClick = { onMonthChanged(displayedMonth.plusMonths(1)) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
        }
    }
}

@Composable
private fun CalendarGrid(
    displayedMonth: LocalDate,
    events: List<CalendarEvent>
) {
    val firstDayOfMonth = displayedMonth.withDayOfMonth(1)
    val firstVisibleDay = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = List(42) { firstVisibleDay.plusDays(it.toLong()) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days) { day ->
            CalendarDayCell(day, displayedMonth.month, events)
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: LocalDate,
    currentMonth: java.time.Month,
    events: List<CalendarEvent>
) {
    val isCurrentMonth = (day.month == currentMonth)
    val dayEvents = events.filter { it.date == day && !it.title.startsWith("Запись к врачу") }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentMonth) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = isCurrentMonth) {},
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(event.color)
                        )
                    }
                    if (dayEvents.size > 3) {
                        Text("+${dayEvents.size - 3}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorAppointmentCard(
    events: List<CalendarEvent>,
    petId: String,
    modifier: Modifier = Modifier,
    onAppointmentAction: (CalendarEvent, Boolean) -> Unit
) {
    var showAppointmentDialog by remember { mutableStateOf(false) }

    val doctorAppointments = events.filter { it.title.startsWith("Запись к врачу:") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Записи к врачу", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (doctorAppointments.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {                     items(doctorAppointments) { appointment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appointment.title + " " +
                                        appointment.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))) + " " +
                                        (appointment.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""),
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { onAppointmentAction(appointment, false) }) {
                                Text("Отменить")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            } else {
                Text("Нет записей", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showAppointmentDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Новая запись")
            }
        }
    }

    if (showAppointmentDialog) {
        DoctorAppointmentDialog(
            events = events,
            petId = petId,
            onDismiss = { showAppointmentDialog = false },
            onConfirm = { newAppointment ->
                onAppointmentAction(newAppointment, true)
                showAppointmentDialog = false
            }
        )
    }
}

@Composable
fun DoctorAppointmentDialog(
    events: List<CalendarEvent>,
    petId: String,
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    // Фиксированный список врачей (для примера)
    val doctors = listOf("Dr. Иванов", "Dr. Петров", "Dr. Сидоров")
    var selectedDoctor by remember { mutableStateOf(doctors.first()) }
    // Фиксированный список целей записи
    val purposes = listOf("Вакцинация", "Осмотр", "Консультация")
    var selectedPurpose by remember { mutableStateOf(purposes.first()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Пример расписания для каждого врача
    val doctorSchedules = mapOf(
        "Dr. Иванов" to listOf(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0)),
        "Dr. Петров" to listOf(LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30)),
        "Dr. Сидоров" to listOf(LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0))
    )
    val availableTimes = doctorSchedules[selectedDoctor] ?: emptyList()

    val appointmentTitle = "Запись к врачу: $selectedDoctor - $selectedPurpose"
    val existingAppointment = events.find {
        it.title == appointmentTitle && it.date == selectedDate && it.time == selectedTime
    }
    val isAvailable = (selectedTime != null && existingAppointment == null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись к врачу") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Выбор даты
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Дата: " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))),
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { showDatePicker = true }) { Text("Выбрать дату") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Выбор врача
                Text("Выберите врача:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    doctors.forEach { doctor ->
                        FilterChip(
                            selected = (selectedDoctor == doctor),
                            onClick = { selectedDoctor = doctor },
                            label = { Text(doctor) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Выбор цели записи
                Text("Цель записи:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    purposes.forEach { purpose ->
                        FilterChip(
                            selected = (selectedPurpose == purpose),
                            onClick = { selectedPurpose = purpose },
                            label = { Text(purpose) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Выбор времени приёма (из доступных слотов)
                Text("Выберите время:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableTimes.forEach { timeSlot ->
                        FilterChip(
                            selected = (selectedTime == timeSlot),
                            onClick = { selectedTime = timeSlot },
                            label = { Text(timeSlot.format(DateTimeFormatter.ofPattern("HH:mm"))) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Статус доступности
                if (selectedTime == null) {
                    Text("Пожалуйста, выберите время", color = Color.Red)
                } else {
                    if (existingAppointment != null) {
                        Text("Слот недоступен для $selectedDoctor", color = Color.Red)
                    } else {
                        Text("Слот доступен", color = Color.Green)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTime != null && existingAppointment == null) {
                        val newAppointment = CalendarEvent(
                            title = appointmentTitle,
                            type = EventType.SINGLE,
                            date = selectedDate,
                            time = selectedTime,
                            repeatMode = null,
                            daysOfWeek = emptyList(),
                            notificationEnabled = false,
                            petId = petId
                        )
                        onConfirm(newAppointment)
                    }
                },
                enabled = selectedTime != null && existingAppointment == null
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showDatePicker) {
        AppointmentDatePicker(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDatePicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите дату записи") },
        text = {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                val selectedMillis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                onDateSelected(date)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun CalendarEventList(
    events: List<CalendarEvent>,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (UUID) -> Unit,
    petId: String
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(events, key = { it.id }) { event ->
            EventCard(
                event = event,
                onEdit = { onEditEvent(it) },
                onDelete = { onDeleteEvent(it) },
                petId = petId,
                modifier = Modifier.animateContentSize()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onEdit: (CalendarEvent) -> Unit,
    onDelete: (UUID) -> Unit,
    petId: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(event.notificationEnabled) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EventHeader(event, expanded) { expanded = !expanded }
            if (expanded) {
                EventDetails(event, notificationEnabled, onEdit, petId)
            }
            EventActions(event, onDelete, onEdit)
        }
    }
}

@Composable
private fun EventHeader(event: CalendarEvent, expanded: Boolean, onExpand: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(
                if (event.type == EventType.REPEATING) R.drawable.ic_repeat else R.drawable.ic_event
            ),
            contentDescription = null,
            tint = event.color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            EventScheduleText(event)
        }
        IconButton(onClick = onExpand) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Подробнее"
            )
        }
    }
}

@Composable
private fun EventScheduleText(event: CalendarEvent) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    Text(
        text = buildString {
            append(event.date.format(dateFormatter))
            event.time?.let { append(", ${it.format(timeFormatter)}") }
            if (event.type == EventType.REPEATING) {
                append(" • ${event.repeatMode?.displayName ?: ""}")
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EventDetails(
    event: CalendarEvent,
    notificationEnabled: Boolean,
    onEdit: (CalendarEvent) -> Unit,
    petId: String
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        NotificationToggle(notificationEnabled, event, onEdit, petId)
        if (event.type == EventType.REPEATING) {
            RepeatBadges(event)
        }
    }
}

@Composable
private fun NotificationToggle(
    enabled: Boolean,
    event: CalendarEvent,
    onEdit: (CalendarEvent) -> Unit,
    petId: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Уведомления", modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = {
                val updatedEvent = event.copy(notificationEnabled = it)
                onEdit(updatedEvent)
                CalendarRepository.updateEvent(petId, updatedEvent)
            }
        )
    }
}

@Composable
private fun RepeatBadges(event: CalendarEvent) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        event.repeatMode?.let {
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text(it.displayName) },
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_repeat), contentDescription = null)
                }
            )
        }
        if (event.daysOfWeek.isNotEmpty()) {
            FilterChip(
                selected = true,
                onClick = {},
                label = {
                    Text(event.daysOfWeek.joinToString {
                        it.getDisplayName(TextStyle.SHORT, Locale("ru"))
                    })
                }
            )
        }
    }
}

@Composable
private fun EventActions(
    event: CalendarEvent,
    onDelete: (UUID) -> Unit,
    onEdit: (CalendarEvent) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        IconButton(onClick = { onEdit(event) }) {
            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
        }
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
    if (showDeleteDialog) {
        DeleteEventDialog(
            onConfirm = { onDelete(event.id) },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteEventDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить событие?") },
        text = { Text("Вы уверены, что хотите удалить это событие?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/** ******************* Диалоги добавления/редактирования ******************* */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    petId: String,
    onDismiss: () -> Unit,
    onAddEvent: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.SINGLE) }
    var repeatMode by remember { mutableStateOf<RepeatMode?>(null) }
    var daysOfWeek by remember { mutableStateOf(emptyList<DayOfWeek>()) }
    var notificationEnabled by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    val dateState = rememberDatePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить событие") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EventTypeSelector(selectedType) { selectedType = it }

                if (selectedType == EventType.REPEATING) {
                    RepeatModeSelector(repeatMode) { repeatMode = it }
                    if (repeatMode == RepeatMode.WEEKLY) {
                        DayOfWeekSelector(daysOfWeek) { daysOfWeek = it }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название события") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                DatePicker(
                    state = dateState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Время: ${selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Не указано"}")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { showTimePicker = true }) {
                        Text("Выбрать время")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Уведомления", modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val date = Instant.ofEpochMilli(dateState.selectedDateMillis ?: System.currentTimeMillis())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val newEvent = CalendarEvent(
                        title = title,
                        type = selectedType,
                        date = date,
                        time = selectedTime,
                        repeatMode = repeatMode,
                        daysOfWeek = daysOfWeek,
                        notificationEnabled = notificationEnabled,
                        petId = petId
                    )
                    onAddEvent(newEvent)
                    onDismiss()
                },
                enabled = title.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { selectedTime = it },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onEditEvent: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var selectedType by remember { mutableStateOf(event.type) }
    var repeatMode by remember { mutableStateOf(event.repeatMode) }
    var daysOfWeek by remember { mutableStateOf(event.daysOfWeek) }
    var notificationEnabled by remember { mutableStateOf(event.notificationEnabled) }
    var selectedTime by remember { mutableStateOf(event.time) }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = event.date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать событие") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EventTypeSelector(selectedType) { selectedType = it }
                if (selectedType == EventType.REPEATING) {
                    RepeatModeSelector(repeatMode) { repeatMode = it }
                    if (repeatMode == RepeatMode.WEEKLY) {
                        DayOfWeekSelector(daysOfWeek) { daysOfWeek = it }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название события") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                DatePicker(
                    state = dateState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Время: ${selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Не указано"}")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { showTimePicker = true }) {
                        Text("Выбрать время")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Уведомления", modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val date = Instant.ofEpochMilli(dateState.selectedDateMillis!!)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val updatedEvent = event.copy(
                        title = title,
                        type = selectedType,
                        date = date,
                        time = selectedTime,
                        repeatMode = repeatMode,
                        daysOfWeek = daysOfWeek,
                        notificationEnabled = notificationEnabled
                    )
                    onEditEvent(updatedEvent)
                    onDismiss()
                },
                enabled = title.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { selectedTime = it },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun EventTypeSelector(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Тип события", style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EventType.values()) { type ->
                FilterChip(
                    selected = (type == selectedType),
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.displayName) }
                )
            }
        }
    }
}

@Composable
private fun RepeatModeSelector(
    selectedMode: RepeatMode?,
    onModeSelected: (RepeatMode?) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Повторение", style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RepeatMode.values()) { mode ->
                FilterChip(
                    selected = (mode == selectedMode),
                    onClick = { onModeSelected(mode) },
                    label = { Text(mode.displayName) }
                )
            }
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    selectedDays: List<DayOfWeek>,
    onDaysSelected: (List<DayOfWeek>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Дни недели", style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DayOfWeek.values()) { day ->
                FilterChip(
                    selected = (day in selectedDays),
                    onClick = {
                        onDaysSelected(
                            if (day in selectedDays) selectedDays - day
                            else selectedDays + day
                        )
                    },
                    label = {
                        Text(day.getDisplayName(TextStyle.SHORT, Locale("ru")))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            androidx.compose.material3.TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                onDismiss()
            }) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
