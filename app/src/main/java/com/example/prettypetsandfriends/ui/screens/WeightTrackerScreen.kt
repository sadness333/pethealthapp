package com.example.prettypetsandfriends.ui.screens

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.data.entities.WeightHistory
import com.example.prettypetsandfriends.backend.repository.WeightRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun WeightTrackerScreen(navController: NavController) {
    val weightRepo = remember { WeightRepository() }
    var weightHistory by remember { mutableStateOf(emptyList<WeightHistory>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newNotes by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val petId = LocalPetState.current.selectedPet?.id

    fun loadData() {
        scope.launch {
            try {
                weightRepo.getWeightHistory(petId).collect { history ->
                    weightHistory = history.sortedByDescending { it.date }
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    LaunchedEffect(petId) {
        loadData()
    }


    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Вес",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { CustomBottomNavigation(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Добавить")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                WeightChart(entries = weightHistory)
                Spacer(Modifier.height(24.dp))
                Text(
                    "История измерений",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            items(weightHistory) { entry ->
                HistoryItem(entry)
            }
        }

    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Новая запись") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Заметки") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                weightRepo.addWeight(
                                    weight = newWeight.toDouble(),
                                    petId = petId,
                                    notes = newNotes
                                )
                                newWeight = ""
                                newNotes = ""
                                showDialog = false
                            } catch (e: Exception) {
                                // Обработка ошибок
                            }
                        }
                    },
                    enabled = newWeight.isNotEmpty()
                ) { Text("Сохранить") }

            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun HistoryItem(entry: WeightHistory) {
    val petId = LocalPetState.current.selectedPet?.id
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val weightRepo = remember { WeightRepository() }
    val primaryColor = colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(entry.date)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Удалить",
                            tint = colorScheme.error
                        )
                    }
                }
                entry.notes.takeIf { it.isNotEmpty() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                }
            }
            Text(
                "%.1f кг".format(entry.value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Вы уверены что хотите удалить эту запись?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                if (petId != null) {
                                    weightRepo.deleteWeight(entry.id, petId)
                                    withContext(Dispatchers.Main) {
                                        showDeleteDialog = false
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Ошибка удаления: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
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


@Composable
private fun WeightChart(entries: List<WeightHistory>) {
    val primaryColor = colorScheme.primary
    val surfaceColor = colorScheme.surface
    val onSurfaceColor = colorScheme.onSurface
    val gridColor = colorScheme.outline.copy(alpha = 0.3f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Динамика веса (кг)",
            style = MaterialTheme.typography.titleMedium,
            color = onSurfaceColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (entries.isEmpty()) {
            EmptyWeightState()
            return@Column
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.surfaceVariant
            )
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val padding = 48.dp.toPx()
                    val graphWidth = size.width - padding * 2
                    val graphHeight = size.height - padding * 2

                    val maxWeight = entries.maxOf { it.value }.let { if (it < 1.0) 1.0 else it }
                    val minWeight = entries.minOf { it.value }.coerceAtLeast(0.0)
                    val yRange = maxWeight - minWeight

                    val gridSteps = 5
                    val gridPaint = Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.RIGHT
                    }

                    repeat(gridSteps + 1) { step ->
                        val yPos = padding + (graphHeight / gridSteps) * step
                        drawLine(
                            color = gridColor,
                            start = Offset(padding, yPos),
                            end = Offset(size.width - padding, yPos),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }

                    repeat(gridSteps + 1) { step ->
                        val value = maxWeight - (yRange / gridSteps) * step
                        val yPos = padding + (graphHeight / gridSteps) * step + 8.dp.toPx()

                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(value),
                            padding - 8.dp.toPx(),
                            yPos,
                            gridPaint
                        )
                    }

                    val path = Path().apply {
                        entries.forEachIndexed { index, entry ->
                            val x = padding + (graphWidth / (entries.size - 1)) * index
                            val y = padding + graphHeight - ((entry.value - minWeight) * (graphHeight / yRange))

                            if (index == 0) moveTo(x, y.toFloat()) else lineTo(x, y.toFloat())
                        }
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    entries.forEachIndexed { index, entry ->
                        val x = padding + (graphWidth / (entries.size - 1)) * index
                        val y = padding + graphHeight - ((entry.value - minWeight) * (graphHeight / yRange))

                        drawCircle(
                            color = surfaceColor,
                            radius = 7.dp.toPx(),
                            center = Offset(x, y.toFloat()),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        drawCircle(
                            color = primaryColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y.toFloat())
                        )
                    }

                    val datePaint = Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }

                    val maxLabels = 6
                    val step = max(1, entries.size / maxLabels)

                    entries.forEachIndexed { index, entry ->
                        if (index % step == 0 || index == entries.lastIndex) {
                            val x = padding + (graphWidth / (entries.size - 1)) * index
                            val date = SimpleDateFormat("dd.MM", Locale.getDefault())
                                .format(Date(entry.date))

                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(
                                -45f,
                                x,
                                size.height - padding + 24.dp.toPx()
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                date,
                                x,
                                size.height - padding + 24.dp.toPx(),
                                datePaint
                            )
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWeightState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_weight),
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Данные о весе отсутствуют",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface.copy(alpha = 0.8f))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Нажмите на кнопку + чтобы добавить\nпервое измерение",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp)
        }
    }
}