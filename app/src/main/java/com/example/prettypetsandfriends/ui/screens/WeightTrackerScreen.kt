package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entites.WeightEntry
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeightTrackerScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }
    val weightEntries = remember { mutableStateListOf(
        WeightEntry(LocalDate.now().minusDays(6), 4.2),
        WeightEntry(LocalDate.now().minusDays(5), 4.3),
        WeightEntry(LocalDate.now().minusDays(4), 4.5),
        WeightEntry(LocalDate.now().minusDays(3), 4.6),
        WeightEntry(LocalDate.now().minusDays(2), 4.7),
        WeightEntry(LocalDate.now().minusDays(1), 4.8),
        WeightEntry(LocalDate.now(), 4.9),
    ) }

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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Добавить запись")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeightChart(entries = weightEntries)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_weight),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Текущий вес: ${weightEntries.last().weight} кг",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Последнее измерение: ${
                                weightEntries.last().date.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                )
                            }",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            items(weightEntries.reversed()) { entry ->
                WeightEntryItem(entry)
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Новое измерение веса") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newWeight,
                            onValueChange = { newWeight = it },
                            label = { Text("Вес (кг)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            weightEntries.add(
                                WeightEntry(
                                    LocalDate.now(),
                                    newWeight.toDouble()
                                )
                            )
                            showDialog = false
                            newWeight = ""
                        },
                        enabled = newWeight.isNotEmpty()
                    ) {
                        Text("Добавить")
                    }
                }
            )
        }
    }
}

@Composable
private fun WeightChart(entries: List<WeightEntry>) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxWeight = entries.maxOf { it.weight }
                val minWeight = entries.minOf { it.weight }
                val yRange = maxWeight - minWeight
                val xStep = size.width / (entries.size - 1)
                val yScale = size.height / yRange

                // Draw grid
                for (i in 0..4) {
                    val yPos = size.height - (size.height / 4 * i)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw line chart
                val path = Path().apply {
                    entries.forEachIndexed { index, entry ->
                        val x = xStep * index
                        val y = size.height - ((entry.weight - minWeight) * yScale)
                        if (index == 0) moveTo(x, y.toFloat()) else lineTo(x, y.toFloat())
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw points
                entries.forEachIndexed { index, entry ->
                    val x = xStep * index
                    val y = size.height - ((entry.weight - minWeight) * yScale)
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y.toFloat())
                    )
                }
            }

            Text(
                text = "Вес",
                modifier = Modifier.align(Alignment.TopStart),
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

@Composable
private fun WeightEntryItem(entry: WeightEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    entry.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                "${entry.weight} кг",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}