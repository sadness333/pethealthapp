package com.example.prettypetsandfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.data.entities.FeedingRecord
import com.example.prettypetsandfriends.data.entities.FeedingTemplate
import com.example.prettypetsandfriends.data.entities.FoodType
import com.example.prettypetsandfriends.data.entities.NutritionData
import com.example.prettypetsandfriends.data.repository.FeedingRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@Composable
fun FeedingScreen(navController: NavController) {
    var feedingRecords by remember { mutableStateOf<List<FeedingRecord>>(emptyList()) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var dailyGoal by remember { mutableStateOf(2000f) }
    var showPetDropdown by remember { mutableStateOf(false) }
    val petId = LocalPetState.current.selectedPet?.id ?: ""

    LaunchedEffect(petId) {
        FeedingRepository.getFeedingRecords(petId) { records ->
            feedingRecords = records
        }

        Firebase.database.getReference("pets/$petId/nutrition/dailyCalories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dailyGoal = snapshot.getValue(Float::class.java) ?: 2000f
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FeedingScreen", "Error loading daily goal", error.toException())
                }
            })
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                showPetDropdown = showPetDropdown,
                onPetClick = { showPetDropdown = true },
                onDismiss = { showPetDropdown = false },
                name = "Питание",
            )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTemplateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить прием еды"
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            CalorieProgressCard(
                current = feedingRecords.sumOf { it.nutrition.calories.toDouble() }.toFloat(),
                goal = dailyGoal,
                onGoalChange = { newGoal ->
                    Firebase.database.getReference("pets/$petId/nutrition/dailyCalories")
                        .setValue(newGoal)
                    dailyGoal = newGoal
                },
                modifier = Modifier.padding(16.dp)
            )

            VetRecommendationsCard(
                petId = petId,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            FeedingHistorySection(
                records = feedingRecords,
                onDelete = { record ->
                    FeedingRepository.deleteFeedingRecord(petId, record.id)
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showTemplateDialog) {
        AddFeedingTemplateDialog(
            onDismiss = { showTemplateDialog = false },
            onConfirm = { newRecord ->
                feedingRecords = feedingRecords + newRecord
            }
        )
    }
}

@Composable
private fun CalorieProgressCard(
    current: Float,
    goal: Float,
    onGoalChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (current / goal).coerceAtLeast(0f)
    val isOverLimit = current > goal
    val statusColor = if (isOverLimit) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    var showGoalDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 12.dp,
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { showGoalDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Дневная норма",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${goal.toInt()} ккал",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isOverLimit) "Превышено на" else "Осталось",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${abs(goal - current).toInt()} ккал",
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Изменить норму",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (showGoalDialog) {
            var newGoal by remember { mutableStateOf(goal.toString()) }

            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                title = { Text("Установить новую норму") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = newGoal,
                            onValueChange = { newGoal = it },
                            label = { Text("Ккал в день") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Рекомендуемые значения:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1500, 2000, 2500).forEach { value ->
                                SuggestionChip(
                                    onClick = { newGoal = value.toString() },
                                    label = {
                                        Text(
                                            "$value ккал",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (newGoal == value.toString())
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            newGoal.toFloatOrNull()?.let {
                                onGoalChange(it)
                                showGoalDialog = false
                            }
                        },
                        enabled = newGoal.toFloatOrNull() != null
                    ) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
private fun VetRecommendationsCard(
    petId: String,
    modifier: Modifier = Modifier
) {
    var recommendations by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(petId) {
        Firebase.database.getReference("pets/$petId/vetRecommendations")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recommendations = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("VetRecommendations", "Error loading data", error.toException())
                    isLoading = false
                }
            })
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рекомендации ветеринара",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                recommendations.isEmpty() -> Text(
                    "Нет рекомендаций",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> recommendations.forEach { rec ->
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pets_black),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedingHistorySection(
    records: List<FeedingRecord>,
    onDelete: (FeedingRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val petId = LocalPetState.current.selectedPet?.id ?: " "
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "История кормлений",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            if (records.isEmpty()) {
                Text(
                    text = "Записей пока нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                records.forEach { record ->
                    FeedingRecordItem(
                        record = record,
                        onDelete = {
                            onDelete(record)
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedingRecordItem(
    record: FeedingRecord,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val iconRes = when (record.type) {
        FoodType.DRY -> R.drawable.ic_dry
        FoodType.WET -> R.drawable.ic_water
        FoodType.HOMEMADE -> R.drawable.ic_eating
    }
    val typeColor = when (record.type) {
        FoodType.DRY -> MaterialTheme.colorScheme.primary
        FoodType.WET -> MaterialTheme.colorScheme.secondary
        FoodType.HOMEMADE -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Тип корма",
                tint = typeColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.foodName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${record.nutrition.calories.toInt()} ккал",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Icon(
                        painter = painterResource(R.drawable.ic_time),
                        contentDescription = "Время",
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = formatFeedingTime(record.feedingTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                if (record.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✎ ${record.comment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление записи") },
            text = { Text("Вы уверены, что хотите удалить эту запись о кормлении?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Новый диалог для выбора шаблона приёма питания
@Composable
private fun AddFeedingTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (FeedingRecord) -> Unit,
) {
    val petState = LocalPetState.current
    val petId = petState.selectedPet?.id ?: ""
    val templates = listOf(
        FeedingTemplate(
            foodName = "Сухой корм",
            type = FoodType.DRY,
            quantity = 100f,
            calories = 350f,
            comment = "Стандартный сухой корм"
        ),
        FeedingTemplate(
            foodName = "Влажный корм",
            type = FoodType.WET,
            quantity = 150f,
            calories = 280f,
            comment = "Для разнообразия"
        ),
        FeedingTemplate(
            foodName = "Домашняя еда",
            type = FoodType.HOMEMADE,
            quantity = 200f,
            calories = 300f,
            comment = "Рецепт от ветеринара"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите шаблон питания") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(template = template) {
                        val formatter = DateTimeFormatter.ISO_DATE_TIME
                        val newRecord = FeedingRecord(
                            foodName = template.foodName,
                            type = template.type,
                            nutrition = NutritionData(
                                calories = template.calories,
                                protein = calculateProtein(template.type, template.quantity),
                                fat = calculateFat(template.type, template.quantity),
                                carbs = calculateCarbs(template.type, template.quantity)
                            ),
                            quantity = template.quantity,
                            feedingTime = LocalDateTime.now().format(formatter),
                            comment = template.comment,
                            petId = petId
                        )
                        FeedingRepository.addFeedingRecord(
                            petId = petId,
                            record = newRecord
                        )
                        onConfirm(newRecord)
                        onDismiss()
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        confirmButton = {} // Подтверждение происходит при выборе шаблона
    )
}

@Composable
private fun TemplateCard(
    template: FeedingTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRes = when (template.type) {
                FoodType.DRY -> R.drawable.ic_dry
                FoodType.WET -> R.drawable.ic_water
                FoodType.HOMEMADE -> R.drawable.ic_eating
            }
            val typeColor = when (template.type) {
                FoodType.DRY -> MaterialTheme.colorScheme.primary
                FoodType.WET -> MaterialTheme.colorScheme.secondary
                FoodType.HOMEMADE -> MaterialTheme.colorScheme.tertiary
            }
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = template.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${template.quantity.toInt()} г • ${template.calories.toInt()} ккал",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (template.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatFeedingTime(feedingTime: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(feedingTime, formatter)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM"))
    } catch (e: Exception) {
        "Некорректная дата"
    }
}

private fun calculateProtein(type: FoodType, quantity: Float) = when(type) {
    FoodType.DRY -> quantity * 0.25f
    FoodType.WET -> quantity * 0.20f
    FoodType.HOMEMADE -> quantity * 0.18f
}

private fun calculateFat(type: FoodType, quantity: Float) = when(type) {
    FoodType.DRY -> quantity * 0.12f
    FoodType.WET -> quantity * 0.10f
    FoodType.HOMEMADE -> quantity * 0.08f
}

private fun calculateCarbs(type: FoodType, quantity: Float) = when(type) {
    FoodType.DRY -> quantity * 0.40f
    FoodType.WET -> quantity * 0.35f
    FoodType.HOMEMADE -> quantity * 0.15f
}
