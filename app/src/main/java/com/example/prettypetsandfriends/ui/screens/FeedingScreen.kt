package com.example.prettypetsandfriends.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.entites.FeedingRecord
import com.example.prettypetsandfriends.data.entites.PetsRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun FeedingScreen(navController: NavController) {
    var feedingRecords by remember { mutableStateOf<List<FeedingRecord>>(dummyFeedingRecords) }
    var showAddFeedingDialog by remember { mutableStateOf(false) }
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
                name = "Питание",
                )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFeedingDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить питание")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(13.dp))
            FeedingChartCard()
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(feedingRecords.size) { index ->
                    val record = feedingRecords[index]
                    FeedingRecordCard(
                        record = record,
                        onDelete = { id ->
                            feedingRecords = feedingRecords.filter { it.id != id }
                        }
                    )
                }
            }
        }
    }

    if (showAddFeedingDialog) {
        AddFeedingDialog(
            onDismiss = { showAddFeedingDialog = false },
            onAddFeeding = { newRecord ->
                feedingRecords = feedingRecords + newRecord
                showAddFeedingDialog = false
            }
        )
    }
}

@Composable
fun FeedingChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "График питания (заглушка)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun FeedingRecordCard(
    record: FeedingRecord,
    onDelete: (UUID) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = record.foodType,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Количество: ${record.quantity} г",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Время: ${record.feedingTime.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"))}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (record.comment.isNotBlank()) {
                Text(
                    text = "Комментарий: ${record.comment}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDelete(record.id) }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingDialog(
    onDismiss: () -> Unit,
    onAddFeeding: (FeedingRecord) -> Unit
) {
    var foodType by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    // Используем текущее время по умолчанию
    var feedingTime by remember { mutableStateOf(LocalDateTime.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить питание") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = foodType,
                    onValueChange = { foodType = it },
                    label = { Text("Тип корма") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Количество (г)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Показываем время кормления и кнопку для обновления (затычка)
                Text(
                    text = "Время кормления: ${feedingTime.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { feedingTime = LocalDateTime.now() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Обновить время")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (foodType.isNotBlank() && quantity.isNotBlank()) {
                    onAddFeeding(
                        FeedingRecord(
                            foodType = foodType,
                            quantity = quantity,
                            feedingTime = feedingTime,
                            comment = comment
                        )
                    )
                }
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private val dummyFeedingRecords = listOf(
    FeedingRecord(
        foodType = "Сухой корм",
        quantity = "100",
        feedingTime = LocalDateTime.now().minusHours(1),
        comment = "Нормальная реакция"
    ),
    FeedingRecord(
        foodType = "Влажный корм",
        quantity = "200",
        feedingTime = LocalDateTime.now().minusHours(5),
        comment = "Немного отказывался"
    )
)
