package com.example.prettypetsandfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.data.entities.Appointment
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.entities.PetEvent
import com.example.prettypetsandfriends.backend.repository.UserRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun MainScreen(navController: NavController) {
    var showPetDropdown by remember { mutableStateOf(false) }
    val petState = LocalPetState.current
    val currentUser = petState.petRepository.getCurrentUser()


    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            petState.loadPets(currentUser.uid)
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                showPetDropdown = showPetDropdown,
                onPetClick = { showPetDropdown = true },
                onDismiss = { showPetDropdown = false },
                name = "Главное меню",
            )
        },
        bottomBar = { CustomBottomNavigation(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ModernPetCareScreen(paddingValues, navController)

        }
    }
}

@Composable
fun ModernPetCareScreen(paddingValues: PaddingValues, navController: NavController) {
    val petState = LocalPetState.current
    val user by UserRepository().observeUserData().collectAsState(initial = null)
    val event by UserRepository().observeNearestEvent(petState).collectAsState(initial = null)



    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Добро пожаловать,",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "${user?.name ?: "Гость"}!",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { QuickActionCard("Документы", R.drawable.ic_doc, onClick = { navController.navigate("document") })}
            item { QuickActionCard("Контроль веса", R.drawable.ic_weight, onClick = {navController.navigate("weight")} ) }
            item { QuickActionCard("Помощь", R.drawable.ic_help, onClick = {navController.navigate("help")} ) }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Мои питомцы",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                TextButton(onClick = { navController.navigate("add_pet") }) {
                    Text("Добавить +")
                }
            }

            if (petState.allPets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "У вас пока нет питомцев. Добавьте первого!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(petState.allPets) { pet ->
                        ModernPetCard(pet = pet, navController = navController)
                    }
                }
            }
        }
        NotifyCard(event = event)
    }
}

@Composable
fun NotifyCard(event: PetEvent?) {
    event?.let {
        val petState = LocalPetState.current
        val petName = petState.allPets.find { it.id == event.petId }?.name ?: "Неизвестный питомец"

        val dateInput = LocalDate.parse(event.date)
        val date = dateInput.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

        Card(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Следующее мероприятие",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$petName: ${event.title} • $date ${event.time}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
@Composable
fun ModernPetCard(pet: Pet, navController: NavController) {
    val appointments = remember { mutableStateListOf<Appointment>() }
    val petId = pet.id
    val lastWeight = pet.weightHistory.values
        .maxByOrNull { it.date }
        ?.value

    LaunchedEffect(petId) {
        Firebase.database.reference.child("appointments")
            .orderByChild("petId").equalTo(petId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointments.clear()
                    for (child in snapshot.children) {
                        val appointment = child.getValue(Appointment::class.java)
                        appointment?.let { appointments.add(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Ошибка: ${error.message}")
                }
            })
    }


    val latestDate = appointments
        .filter { it.status == "completed" }
        .maxOfOrNull {
            LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE)
        }?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Нет данных"

    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { navController.navigate("pet_profile/${pet.id}") },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = pet.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${pet.age}, порода: ${pet.breed}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoChip("Вес", "$lastWeight кг")
                InfoChip("Последний визит" +
                        " у врача", latestDate)
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionCard(title: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

