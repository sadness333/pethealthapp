package com.example.prettypetsandfriends.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entites.HealthRecord
import com.example.prettypetsandfriends.data.entites.PetsRepository
import com.example.prettypetsandfriends.data.entites.RecordType
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar

object HealthRecordsRepository {
    fun getSampleRecords(): List<HealthRecord> = listOf(
        HealthRecord(
            date = "20 октября 2023",
            title = "Плановое взвешивание",
            description = "Текущий вес: 4.8 кг\nИдеальный вес для породы: 5.0 кг",
            type = RecordType.WEIGHT,
        ),
        HealthRecord(
            date = "18 октября 2023",
            title = "Комплексная вакцинация",
            description = "Вакцина: Nobivac Tricat Trio\nСледующая прививка: через 1 год",
            type = RecordType.VACCINATION
        ),
        HealthRecord(
            date = "15 октября 2023",
            title = "Назначение лекарств",
            description = "Лакомство с таурином\n2 раза в день после еды",
            type = RecordType.MEDICATION,
        ),
        HealthRecord(
            date = "12 октября 2023",
            title = "Проблемы с аппетитом",
            description = "Отказ от еды в течение суток\nРекомендации: консультация ветеринара",
            type = RecordType.SYMPTOM
        )
    )
}


@Composable
fun HealthDiaryScreen(navController: NavController) {
    val records = remember { HealthRecordsRepository.getSampleRecords() }
    var showPetDropdown by remember { mutableStateOf(false) }
    val pets = remember { PetsRepository.pets }

    Scaffold(
            topBar = {
                CustomTopBar(
                    navController = navController,
                    showPetDropdown = showPetDropdown,
                    onPetClick = { showPetDropdown = true },
                    onDismiss = { showPetDropdown = false },
                    pets = pets
                )
            },
            bottomBar = { CustomBottomNavigation(navController) }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            items(records) { record ->
                Spacer(modifier = Modifier.height(13.dp))
                HealthRecordCard(record = record)
            }
        }
    }
}

@Composable
fun HealthRecordCard(record: HealthRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                 /* Icon(
                    imageVector = when(record.type) {
                        RecordType.WEIGHT -> Icons.Default.MonitorWeight
                        RecordType.VACCINATION -> Icons.Default.MedicalServices
                        RecordType.MEDICATION -> Icons.Default.Medication
                        RecordType.SYMPTOM -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) */
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = record.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier
                .height(8.dp)
            )

            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (record.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(record.photos) { photoRes ->
                        Image(
                            painter = painterResource(id = photoRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}