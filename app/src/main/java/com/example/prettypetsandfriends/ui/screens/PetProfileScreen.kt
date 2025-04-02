package com.example.prettypetsandfriends.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.repository.HealthRecordsRepository
import com.example.prettypetsandfriends.backend.repository.StorageRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.example.prettypetsandfriends.ui.components.DatePickerSection
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.data.entities.Appointment
import com.example.prettypetsandfriends.data.entities.HealthRecord
import com.example.prettypetsandfriends.data.entities.Pet
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PetProfileScreen(petId: String, navController: NavController) {
    val petState = LocalPetState.current
    val pet = remember(petId) { petState.getPetById(petId) }
    val appointments = remember { mutableStateListOf<Appointment>() }
    val context = LocalContext.current
    val database = Firebase.database.reference
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentPet by remember { mutableStateOf(petState.getPetById(petId)) }

    LaunchedEffect(petId) {
        database.child("pets").child(petId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentPet = snapshot.getValue(Pet::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    LaunchedEffect(petId) {
        Firebase.database.reference.child("appointments")
            .orderByChild("petId").equalTo(petId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointments.clear()
                    for (child in snapshot.children) {
                        child.getValue(Appointment::class.java)?.let {
                            appointments.add(it)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Ошибка загрузки записей", Toast.LENGTH_SHORT).show()
                }
            })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление питомца") },
            text = { Text("Вы уверены, что хотите удалить ${currentPet?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        database.child("pets").child(petId).removeValue()
                        navController.popBackStack()
                        Toast.makeText(context, "Питомец удалён", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showEditDialog && currentPet != null) {
        var name by remember { mutableStateOf(currentPet!!.name) }
        var breed by remember { mutableStateOf(currentPet!!.breed) }
        var birthYear by remember { mutableStateOf(currentPet!!.birthYear) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var newImageUrl by remember { mutableStateOf(currentPet!!.photoUrl) }
        val storageRepository = remember { StorageRepository() }
        val coroutineScope = rememberCoroutineScope()

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактирование профиля") },
            text = {
                Column(Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Порода") }
                    )
                    Spacer(Modifier.height(8.dp))
                    DatePickerSection(
                        selectedDate = birthYear,
                        onDateSelected = { birthYear = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = "Выбранное изображение",
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(150.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Выбрать фото")
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(newImageUrl),
                                    contentDescription = "Текущее изображение",
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(150.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Выбрать фото")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            coroutineScope.launch {
                                try {
                                    val url = storageRepository.uploadPetImage(currentPet!!.ownerId, selectedImageUri!!)
                                    newImageUrl = url
                                    val updates = hashMapOf<String, Any>(
                                        "name" to name,
                                        "breed" to breed,
                                        "birthYear" to birthYear,
                                        "photoUrl" to (newImageUrl ?: "https://cdn-icons-png.flaticon.com/128/4225/4225935.png")
                                    )
                                    database.child("pets").child(petId).updateChildren(updates)
                                        .addOnSuccessListener {
                                            currentPet = currentPet?.copy(
                                                name = name,
                                                breed = breed,
                                                birthYear = birthYear,
                                                photoUrl = newImageUrl
                                            )
                                            showEditDialog = false
                                            Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                                        }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val updates = hashMapOf<String, Any>(
                                "name" to name,
                                "breed" to breed,
                                "birthYear" to birthYear
                            )
                            database.child("pets").child(petId).updateChildren(updates)
                                .addOnSuccessListener {
                                    currentPet = currentPet?.copy(
                                        name = name,
                                        breed = breed,
                                        birthYear = birthYear
                                    )
                                    showEditDialog = false
                                    Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Профиль питомца",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { CustomBottomNavigation(navController) }
    ) { paddingValues ->
        if (pet == null) {
            EmptyState(
                title = "Питомец не найден",
                subtitle = "Попробуйте обновить страницу или проверьте подключение",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            PetContent(
                pet = pet,
                onEditClick = { showEditDialog = true },
                onDeleteClick = { showDeleteDialog = true },
                appointments = appointments,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PetContent(
    pet: Pet,
    appointments: List<Appointment>,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val healthRecords = remember { mutableStateListOf<HealthRecord>() }
    val recordsRepository = remember { HealthRecordsRepository() }

    LaunchedEffect(pet.id) {
        recordsRepository.getHealthRecordsFlow(pet.id)
            .collect { records ->
                healthRecords.clear()
                healthRecords.addAll(records)
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileHeader(pet, onEditClick, onDeleteClick) }
        item { MedicalInfoCard(pet, appointments) }
        item { VaccinationCard(healthRecords) }
    }
}

@Composable
private fun ProfileHeader(
    pet: Pet,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pet.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = pet.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${pet.age}, порода: ${pet.breed}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FilledTonalButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Изменить",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }

                FilledTonalButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Удалить",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalInfoCard(pet: Pet, appointments: List<Appointment>) {
    val latestWeight = pet.weightHistory.values
        .maxByOrNull { it.date }
        ?.value
        ?.let { "%.1f кг".format(it) }
        ?: "Нет данных"

    val latestVisit = appointments
        .filter { it.status == "completed" }
        .maxOfOrNull {
            LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE)
        }?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Нет данных"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            SectionTitle("Медицинские показатели")

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            MedicalInfoItem(
                icon = R.drawable.ic_weight,
                title = "Вес",
                value = latestWeight,
                modifier = Modifier.padding(top = 16.dp)
            )

            MedicalInfoItem(
                icon = R.drawable.ic_event,
                title = "Последний визит к врачу",
                value = latestVisit
            )
        }
    }
}

@Composable
private fun VaccinationCard(healthRecords: List<HealthRecord>) {
    val vaccinations = healthRecords
        .filter { it.type == "VACCINATION" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            SectionTitle("Вакцинации")

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (vaccinations.isEmpty()) {
                EmptyState(
                    title = "Нет данных о прививках",
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
            } else {
                vaccinations.forEachIndexed { index, record ->
                    val formattedDate = record.date.toEuropeanDate()
                    val expirationDate = record.expirationDate.toEuropeanDate()

                    VaccinationItem(
                        name = record.title,
                        date = formattedDate,
                        expiration = expirationDate,
                        isLast = index == vaccinations.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalInfoItem(
    icon: Int,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun VaccinationItem(
    name: String,
    date: String,
    expiration: String,
    isLast: Boolean
) {
    Spacer(Modifier.height(24.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Vaccines,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Дата: $date",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                if (expiration.isNotEmpty()) {
                    Text(
                        text = "Действует до: $expiration",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (!isLast) {
            Divider(
                modifier = Modifier
                    .padding(top = 12.dp, start = 40.dp)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )

        subtitle?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        }
    }
}
