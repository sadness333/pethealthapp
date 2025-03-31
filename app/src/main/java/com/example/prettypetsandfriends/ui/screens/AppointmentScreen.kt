package com.example.prettypetsandfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.data.entities.Appointment
import com.example.prettypetsandfriends.data.entities.SlotStatus
import com.example.prettypetsandfriends.data.entities.TimeSlot
import com.example.prettypetsandfriends.data.entities.Vet
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AppointmentScreen(navController: NavController) {
    val context = LocalContext.current
    val vets = remember { mutableStateOf<List<Vet>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val reasons = listOf("Вакцинация", "Осмотр", "Консультация", "Лечение")
    val dates = remember { List(14) { LocalDate.now().plusDays(it.toLong()) } }
    var selectedVet by remember { mutableStateOf<Vet?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var notes by remember { mutableStateOf("") }
    val petId = LocalPetState.current.selectedPet?.id ?: ""
    val currentUser = LocalPetState.current.petRepository.getCurrentUser()


    LaunchedEffect(selectedVet) {
        selectedDate = LocalDate.now()
        selectedTime = null
    }

    LaunchedEffect(Unit) {
        Firebase.database.reference.child("doctors")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vets.value = snapshot.children.mapNotNull {
                        it.getValue(Vet::class.java)?.copy(id = it.key ?: "")
                    }
                    isLoading.value = false
                }
                override fun onCancelled(error: DatabaseError) {
                    isLoading.value = false
                }
            })
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Запись к врачу",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                VetSelectionCard(
                    vets = vets.value,
                    selectedVet = selectedVet,
                    onVetSelected = { selectedVet = it }
                )

                DateSelector(
                    dates = dates,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )

                selectedVet?.let { vet ->
                    if (!isWorkDay(vet, selectedDate)) {
                        InfoCard("Врач не работает в выбранную дату")
                    } else {
                        TimeScheduleCard(
                            vet = vet,
                            selectedDate = selectedDate,
                            selectedTime = selectedTime,
                            onTimeSelected = { selectedTime = it }
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Причина посещения:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(reasons) { reason ->
                                FilterChip(
                                    selected = (reason == selectedReason),
                                    onClick = { selectedReason = reason },
                                    enabled = true,
                                    label = {
                                        Text(
                                            reason,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = (reason == selectedReason),
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        selectedBorderColor = Color.Transparent,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        disabledSelectedBorderColor = Color.Transparent,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(40.dp)
                                )

                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Дополнительные заметки",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (validateForm(selectedVet, selectedTime)) {
                            if (currentUser != null) {
                                saveAppointment(
                                    vet = selectedVet!!,
                                    petId = petId,
                                    ownerId = currentUser.uid,
                                    date = selectedDate,
                                    time = selectedTime!!,
                                    reason = selectedReason,
                                    notes = notes,
                                    onSuccess = {
                                        navController.popBackStack()
                                        Toast.makeText(context, "Запись создана!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = {
                                        Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Сохранить запись", modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
private fun VetSelectionCard(
    vets: List<Vet>,
    selectedVet: Vet?,
    onVetSelected: (Vet) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Выберите врача", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vets) { vet ->
                    VetCard(
                        vet = vet,
                        isSelected = vet.id == selectedVet?.id,
                        onClick = { onVetSelected(vet) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VetCard(vet: Vet, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = vet.photo,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                vet.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                vet.specialization,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateSelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "Выберите дату",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp),

                )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(dates) { date ->
                    DateCard(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateCard(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(dayOfWeek, style = MaterialTheme.typography.labelMedium)
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge)
            Text(date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TimeScheduleCard(
    vet: Vet,
    selectedDate: LocalDate,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val appointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    LaunchedEffect(vet.id, selectedDate) {
        Firebase.database.reference.child("appointments")
            .orderByChild("doctorId").equalTo(vet.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointments.value = snapshot.children.mapNotNull {
                        it.getValue(Appointment::class.java)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    val slots = remember(selectedDate, appointments.value) {
        generateTimeSlots(vet, selectedDate, appointments.value)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(max = 400.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Выберите время",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(slots) { slot ->
                    TimeSlotCard(
                        slot = slot,
                        isSelected = slot.time == selectedTime,
                        onClick = { onTimeSelected(slot.time) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSlotCard(slot: TimeSlot, isSelected: Boolean, onClick: () -> Unit) {
    val (containerColor, contentColor) = when {
        isSelected -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        slot.status == SlotStatus.AVAILABLE -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = slot.status == SlotStatus.AVAILABLE, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = slot.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelLarge
                )
                if (slot.status != SlotStatus.AVAILABLE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Занято",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


@Composable
private fun InfoCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}


private fun validateForm(vet: Vet?, time: LocalTime?): Boolean {
    return vet != null && time != null
}

private fun generateTimeSlots(
    vet: Vet,
    date: LocalDate,
    appointments: List<Appointment>
): List<TimeSlot> {
    if (!isWorkDay(vet, date)) return emptyList()

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val bookedTimes = appointments
        .filter {
            runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() == date
        }
        .map {
            runCatching { LocalTime.parse(it.time) }.getOrNull()
        }
        .filterNotNull()

    val start = LocalTime.parse(vet.base_schedule.start)
    val end = LocalTime.parse(vet.base_schedule.end)
    val slotDuration = vet.base_schedule.slotDuration

    return generateSequence(start) { it.plusMinutes(slotDuration.toLong()) }
        .takeWhile { it.isBefore(end) }
        .map { time ->
            when {
                bookedTimes.contains(time) -> TimeSlot(time, SlotStatus.BOOKED)
                else -> TimeSlot(time, SlotStatus.AVAILABLE)
            }
        }
        .toList()
}

private fun isWorkDay(vet: Vet, date: LocalDate): Boolean {
    val dayOfWeek = date.dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        .lowercase()
    return vet.working_days.contains(dayOfWeek)
}

private fun saveAppointment(
    vet: Vet,
    petId: String,
    ownerId: String,
    date: LocalDate,
    time: LocalTime,
    reason: String,
    notes: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val appointmentId = Firebase.database.reference.child("appointments").push().key ?: ""
    val createdAt = LocalDateTime.now()
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ISO_INSTANT)

    val appointment = Appointment(
        id = appointmentId,
        doctorId = vet.id,
        petId = petId,
        ownerId = ownerId,
        date = date.toString(),
        time = time.toString(),
        reason = reason,
        notes = notes,
        createdAt = createdAt
    )

    Firebase.database.reference.child("appointments/$appointmentId").setValue(appointment)
        .addOnSuccessListener {
            val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))

            val currentDateTime = LocalDateTime.now()
            val appointmentDateTime = LocalDateTime.of(
                date,
                LocalTime.of(
                    time.hour,
                    time.minute,
                    currentDateTime.second,
                    currentDateTime.nano
                )
            )
            val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            val formattedTimestamp = appointmentDateTime.format(timestampFormatter)

            val slotData = hashMapOf(
                "appointmentId" to appointmentId,
                "status" to "pending",
                "timestamp" to formattedTimestamp
            )

            val slotPath = "doctors/${vet.id}/schedule/$formattedDate/$formattedTime"

            Firebase.database.reference.child(slotPath).updateChildren(slotData as Map<String, Any>)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError() }
        }
        .addOnFailureListener { onError() }
}