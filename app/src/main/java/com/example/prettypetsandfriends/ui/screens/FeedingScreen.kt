package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entities.FeedingRecord
import com.example.prettypetsandfriends.data.entities.FoodType
import com.example.prettypetsandfriends.data.entities.NutritionData
import com.example.prettypetsandfriends.data.entities.PetsRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun FeedingScreen(navController: NavController) {
    var feedingRecords by remember { mutableStateOf(dummyFeedingRecords) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var dailyGoal by remember { mutableStateOf(2000f) }
    var showPetDropdown by remember { mutableStateOf(false) }
    val pets = remember { PetsRepository.pets }

    val dailyCalories = remember(feedingRecords) {
        feedingRecords.sumOf { it.nutrition.calories.toDouble() }.toFloat()
    }

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
                onClick = { showAddDialog = true },
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
                current = dailyCalories,
                goal = dailyGoal,
                onGoalChange = { newGoal -> dailyGoal = newGoal },
                modifier = Modifier.padding(16.dp)
            )

            VetRecommendationsCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            FeedingHistorySection(
                records = feedingRecords,
                onDelete = { feedingRecords = feedingRecords - it },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showAddDialog) {
        AddFeedingDialog(
            onDismiss = { showAddDialog = false },
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
private fun VetRecommendationsCard(modifier: Modifier = Modifier) {
    val recommendations = listOf(
        "Ежедневно проверяйте наличие свежей воды",
        "Контролируйте вес питомца раз в неделю",
        "Избегайте резкой смены корма"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рекомендации ветеринара",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            recommendations.forEach { rec ->
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

@Composable
private fun FeedingHistorySection(
    records: List<FeedingRecord>,
    onDelete: (FeedingRecord) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        onDelete = { onDelete(record) },
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.foodName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = record.feedingTime.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = "${record.nutrition.calories.toInt()} ккал",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (record.comment.isNotBlank()) {
                Text(
                    text = "Комментарий: ${record.comment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AddFeedingDialog(
    onDismiss: () -> Unit,
    onConfirm: (FeedingRecord) -> Unit,
) {
    var foodName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FoodType.DRY) }
    var calories by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var feedingTime by remember { mutableStateOf(LocalDateTime.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить прием питания") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Название корма") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FoodType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(type.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .defaultMinSize(
                                    minWidth = 64.dp,
                                    minHeight = 40.dp
                                )
                                .padding(vertical = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Калории") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Кол-во (г)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = foodName.isNotBlank() && calories.isNotBlank() && quantity.isNotBlank(),
                onClick = {
                    onConfirm(
                        FeedingRecord(
                            foodName = foodName,
                            type = selectedType,
                            nutrition = NutritionData(
                                calories = calories.toFloat(),
                                protein = calculateProtein(selectedType, quantity.toFloat()),
                                fat = calculateFat(selectedType, quantity.toFloat()),
                                carbs = calculateCarbs(selectedType, quantity.toFloat())
                            ),
                            quantity = quantity.toFloat(),
                            feedingTime = feedingTime,
                            comment = comment
                        )
                    )
                    onDismiss()
                }
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

private val dummyFeedingRecords = listOf(
            FeedingRecord(
                foodName = "Royal Canin",
                type = FoodType.DRY,
                nutrition = NutritionData(350f, 25f, 12f, 40f),
                quantity = 100f,
                feedingTime = LocalDateTime.now().minusHours(2),
                comment = "Хороший аппетит"
            )
        )