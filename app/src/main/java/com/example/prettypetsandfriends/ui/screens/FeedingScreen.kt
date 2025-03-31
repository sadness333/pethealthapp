package com.example.prettypetsandfriends.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.backend.PetState
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun FeedingScreen(navController: NavController) {
    val context = LocalContext.current
    var feedingRecords by remember { mutableStateOf<List<FeedingRecord>>(emptyList()) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showTemplateConstructorDialog by remember { mutableStateOf(false) }
    var dailyGoal by remember { mutableStateOf(2000f) }
    var showPetDropdown by remember { mutableStateOf(false) }
    val petState = LocalPetState.current
    val petId = petState.selectedPet?.id ?: ""
    val hasPets = petState.allPets.isNotEmpty()


    var feedingTemplates by remember { mutableStateOf(emptyList<FeedingTemplate>()) }


    LaunchedEffect(petId) {
        if (petId.isEmpty()) return@LaunchedEffect
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

    LaunchedEffect(petId) {
        if (petId.isEmpty()) return@LaunchedEffect
        Firebase.database.getReference("pets/$petId/nutrition/templates")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val templatesFromFirebase = snapshot.children.mapNotNull { child ->
                        child.getValue(FeedingTemplate::class.java)?.copy(id = child.key ?: "")
                    }
                    feedingTemplates = templatesFromFirebase
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FeedingScreen", "Error loading templates", error.toException())
                }
            })
    }

    val currentDayCalories = feedingRecords
        .filter { record ->
            val recordDate = LocalDateTime.parse(record.feedingTime, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
            recordDate == LocalDate.now()
        }
        .sumOf { it.nutrition.calories.toDouble() }.toFloat()

    val sortedRecords = feedingRecords.sortedByDescending {
        LocalDateTime.parse(it.feedingTime, DateTimeFormatter.ISO_DATE_TIME)
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                showPetDropdown = showPetDropdown,
                onPetClick = { showPetDropdown = true },
                onDismiss = { showPetDropdown = false },
                name = "Питание"
            )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (petState.allPets.isEmpty()) {
                        Toast.makeText(context, "Отсутствуют животные для добавления шаблонов питания", Toast.LENGTH_LONG).show()
                    } else{
                        showTemplateDialog = true
                    }},
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить прием еды")
            }
        }
    ) { padding ->
        if (!hasPets) {
            NoPetsPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            CalorieProgressCard(
                current = currentDayCalories,
                goal = dailyGoal,
                onGoalChange = { newGoal ->
                    Firebase.database.getReference("pets/$petId/nutrition/dailyCalories").setValue(newGoal)
                    dailyGoal = newGoal
                },
                modifier = Modifier.padding(16.dp),
                petState = petState,
                context = context
            )

            VetRecommendationsCard(
                petId = petId,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            FeedingHistorySection(
                records = sortedRecords,
                onDelete = { record ->
                    FeedingRepository.deleteFeedingRecord(petId, record.id)
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showTemplateDialog) {
        AddFeedingTemplateDialog(
            templates = feedingTemplates,
            onAddNewTemplate = { showTemplateConstructorDialog = true },
            onDeleteTemplate = { template ->
                Firebase.database.getReference("pets/$petId/nutrition/templates")
                    .child(template.id).removeValue()
            },
            onDismiss = { showTemplateDialog = false },
            onConfirm = { newRecord ->
                feedingRecords = feedingRecords + newRecord
            }
        )
    }

    if (showTemplateConstructorDialog) {
        TemplateConstructorDialog(
            onDismiss = { showTemplateConstructorDialog = false },
            onTemplateCreated = { newTemplate ->
                Firebase.database.getReference("pets/$petId/nutrition/templates")
                    .push().setValue(newTemplate)
                showTemplateConstructorDialog = false
            }
        )
    }
}

@Composable
private fun NoPetsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pets_black),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Добавьте питомца",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Для работы с разделом питания необходимо сначала добавить питомца в профиль",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun CalorieProgressCard(
    current: Float,
    goal: Float,
    onGoalChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    petState: PetState,
    context: Context
) {
    val progress = (current / goal).coerceAtLeast(0f)
    val isOverLimit = current > goal
    val statusColor = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    var showGoalDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
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
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { if (petState.allPets.isEmpty()) {
                    Toast.makeText(context, "Невозможно поменять дневную норму у несуществующего питомца", Toast.LENGTH_LONG).show()
                } else {
                    showGoalDialog = true
                }},
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Дневная норма", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${goal.toInt()} ккал", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isOverLimit) "Превышено на" else "Осталось", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${abs(goal - current).toInt()} ккал", style = MaterialTheme.typography.titleMedium, color = statusColor, fontWeight = FontWeight.Bold)
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
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = newGoal,
                            onValueChange = { newGoal = it },
                            label = { Text("Ккал в день") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Рекомендуемые значения:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1500, 2000, 2500).forEach { value ->
                                SuggestionChip(
                                    onClick = { newGoal = value.toString() },
                                    label = { Text("$value ккал", style = MaterialTheme.typography.labelSmall) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (newGoal == value.toString()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
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
                    ) { Text("Сохранить") }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalDialog = false }) { Text("Отмена") }
                }
            )
        }
    }
}

@Composable
private fun VetRecommendationsCard(petId: String, modifier: Modifier = Modifier) {
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
            Text("Рекомендации ветеринара", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                recommendations.isEmpty() -> Text("Нет рекомендаций", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> recommendations.forEach { rec ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_pets_black), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(rec, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedingHistorySection(records: List<FeedingRecord>, onDelete: (FeedingRecord) -> Unit, modifier: Modifier = Modifier) {
    val petId = LocalPetState.current.selectedPet?.id ?: " "
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("История кормлений", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            if (records.isEmpty()) {
                Text("Записей пока нет", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            } else {
                records.forEach { record ->
                    FeedingRecordItem(record = record, onDelete = { onDelete(record) }, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedingRecordItem(record: FeedingRecord, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateTime = LocalDateTime.parse(record.feedingTime, DateTimeFormatter.ISO_DATE_TIME)

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
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = iconRes), contentDescription = "Тип корма", tint = typeColor, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateTime.format(DateTimeFormatter.ofPattern("dd MMM")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        text = record.foodName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionBadge(
                        value = record.nutrition.calories,
                        unit = "ккал",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.padding(start = 8.dp)) {
                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление записи") },
            text = { Text("Вы уверены, что хотите удалить эту запись о кормлении?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Да", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun NutritionBadge(value: Float, unit: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${value.toInt()} $unit",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TemplateCard(
    template: FeedingTemplate,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = typeColor, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.foodName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${template.quantity.toInt()} г • ${template.calories.toInt()} ккал", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (template.presentation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Форма: ${template.presentation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (template.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(template.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = "Удалить шаблон", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NumericStepper(
    value: Float,
    onValueChange: (Float) -> Unit,
    step: Float,
    min: Float,
    max: Float,
    label: String
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange((value - step).coerceAtLeast(min)) },
                enabled = value > min
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Уменьшить")
            }
            Text(text = "${value.toInt()}", style = MaterialTheme.typography.headlineSmall)
            IconButton(
                onClick = { onValueChange((value + step).coerceAtMost(max)) },
                enabled = value < max
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Увеличить")
            }
        }
    }
}

@Composable
private fun AddFeedingTemplateDialog(
    templates: List<FeedingTemplate>,
    onAddNewTemplate: () -> Unit,
    onDeleteTemplate: (FeedingTemplate) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (FeedingRecord) -> Unit,
) {
    val petState = LocalPetState.current
    val petId = petState.selectedPet?.id ?: ""
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите шаблон питания") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(templates) { template ->
                        TemplateCard(template = template,
                            onClick = {
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
                                FeedingRepository.addFeedingRecord(petId, newRecord)
                                onConfirm(newRecord)
                                onDismiss()
                            },
                            onDelete = { onDeleteTemplate(template) }
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddNewTemplate() }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Добавить новый шаблон", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun TemplateConstructorDialog(
    onDismiss: () -> Unit,
    onTemplateCreated: (FeedingTemplate) -> Unit
) {
    var selectedType by remember { mutableStateOf(FoodType.DRY) }
    var foodName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var presentation by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(100f) }
    var calories by remember { mutableStateOf(350f) }

    LaunchedEffect(selectedType) {
        foodName = when (selectedType) {
            FoodType.DRY -> "Сухой корм"
            FoodType.WET -> "Влажный корм"
            FoodType.HOMEMADE -> "Домашняя еда"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать новый шаблон питания") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                Text("Тип питания", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FoodType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(type.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (selectedType == type)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Название корма") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NumericStepper(
                        value = quantity,
                        onValueChange = { quantity = it },
                        step = 50f,
                        min = 50f,
                        max = 500f,
                        label = "Кол-во (г)"
                    )
                    NumericStepper(
                        value = calories,
                        onValueChange = { calories = it },
                        step = 50f,
                        min = 100f,
                        max = 1000f,
                        label = "Калории (ккал)"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = presentation,
                    onValueChange = { presentation = it },
                    label = { Text("Форма подачи (например, квадратики)") },
                    modifier = Modifier.fillMaxWidth()
                )
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
            Button(onClick = {
                val newTemplate = FeedingTemplate(
                    foodName = foodName,
                    type = selectedType,
                    quantity = quantity,
                    calories = calories,
                    comment = comment,
                    presentation = presentation
                )
                onTemplateCreated(newTemplate)
                onDismiss()
            }) { Text("Сохранить шаблон") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
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

private fun calculateProtein(type: FoodType, quantity: Float) = when (type) {
    FoodType.DRY -> quantity * 0.25f
    FoodType.WET -> quantity * 0.20f
    FoodType.HOMEMADE -> quantity * 0.18f
}

private fun calculateFat(type: FoodType, quantity: Float) = when (type) {
    FoodType.DRY -> quantity * 0.12f
    FoodType.WET -> quantity * 0.10f
    FoodType.HOMEMADE -> quantity * 0.08f
}

private fun calculateCarbs(type: FoodType, quantity: Float) = when (type) {
    FoodType.DRY -> quantity * 0.40f
    FoodType.WET -> quantity * 0.35f
    FoodType.HOMEMADE -> quantity * 0.15f
}
