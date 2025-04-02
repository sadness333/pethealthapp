package com.example.prettypetsandfriends.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.repository.HealthRecordsRepository
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.data.entities.Appointment
import com.example.prettypetsandfriends.data.entities.HealthRecord
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun PetProfileScreen(petId: String, navController: NavController) {
    val petState = LocalPetState.current
    val pet = remember(petId) { petState.getPetById(petId) }
    val appointments = remember { mutableStateListOf<Appointment>() }
    val context = LocalContext.current

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
                appointments = appointments,
                modifier = Modifier.padding(paddingValues))        }
    }
}

@Composable
private fun PetContent(pet: Pet, appointments: List<Appointment>, modifier: Modifier = Modifier) {
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
        item { ProfileHeader(pet) }
        item { MedicalInfoCard(pet, appointments) }
        item { VaccinationCard(healthRecords) }
    }
}

@Composable
private fun ProfileHeader(pet: Pet) {
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
                value = latestWeight ,
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
                    horizontalAlignment = Alignment.CenterHorizontally)
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
            tint = MaterialTheme.colorScheme.primary,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Vaccines,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(text = "Дата: $date", style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary
                ))
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
            color = MaterialTheme.colorScheme.primary,
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
