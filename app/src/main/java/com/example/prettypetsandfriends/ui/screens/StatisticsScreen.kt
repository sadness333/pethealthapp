package com.example.prettypetsandfriends.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(navController: NavController) {
    val database = FirebaseDatabase.getInstance().reference
    val petId = LocalPetState.current.selectedPet?.id
    var petStats by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var weightHistory by remember { mutableStateOf<List<Double>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(petId) {
        val listeners = mutableListOf<ValueEventListener>()

        petId?.let {
            val statsRef = database.child("pets").child(it).child("statistics")
            val statsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    petStats = snapshot.value as? Map<String, Any> ?: emptyMap()
                    if (weightHistory.isNotEmpty()) loading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    loading = false
                }
            }
            statsRef.addValueEventListener(statsListener)
            listeners.add(statsListener)

            val weightRef = database.child("pets").child(it).child("weightHistory")
            val weightListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    weightHistory = snapshot.children.mapNotNull {
                        it.child("value").getValue(Double::class.java)
                    }
                    if (petStats.isNotEmpty()) loading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    loading = false
                }
            }
            weightRef.addValueEventListener(weightListener)
            listeners.add(weightListener)
        }

        onDispose {
            listeners.forEach { listener ->
                petId?.let {
                    database.child("pets").child(it).removeEventListener(listener)
                }
            }
        }
    }

    UI(navController, loading, petStats, weightHistory)
}

@Composable
private fun UI(
    navController: NavController,
    loading: Boolean,
    stats: Map<String, Any>,
    weightHistory: List<Double>
) {
    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Статистика",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { CustomBottomNavigation(navController) }
    ) { padding ->
        // Добавляем вертикальную прокрутку
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            when {
                loading -> FullScreenLoader()
                stats.isEmpty() -> ErrorMessage("Нет данных о питомце")
                else -> StatisticsCards(calculateMetrics(stats, weightHistory))
            }
        }
    }
}

@Composable
private fun StatisticsCards(metrics: Map<String, String>) {
    metrics.forEach { (title, value) ->
        StatisticItem(title, value)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatisticItem(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun calculateMetrics(
    stats: Map<String, Any>,
    weightHistory: List<Double>
): Map<String, String> = mutableMapOf<String, String>().apply {
    put("Средний балл здоровья", stats["avgHealthScore"].safeToString())
    put("Текущий вес", "${stats["lastWeight"].safeToString()} кг")
    put("Вакцинаций", stats["vaccinationsCount"].safeToString())
    put("Регулярность кормления", "${stats["feedingConsistency"].safeToString()}%")

    val avgWeight = if (weightHistory.isNotEmpty()) {
        weightHistory.average()
    } else {
        stats["avgWeight"]?.toString()?.toDoubleOrNull() ?: 0.0
    }
    put("Средний вес", "%.1f кг".format(avgWeight))
    put("Последний визит", parseTimestamp(stats["lastVetVisit"]))
}

private fun Any?.safeToString(): String = this?.toString() ?: "—"

private fun parseTimestamp(timestamp: Any?): String {
    return when (timestamp) {
        is Long -> formatDate(timestamp)
        is String -> timestamp.toLongOrNull()?.let { formatDate(it) } ?: "Неверный формат"
        else -> "Отсутствует"
    }
}

private fun formatDate(timeInMillis: Long): String {
    if (timeInMillis.toDouble() != 0.0)  {
        return try {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timeInMillis))
        } catch (e: Exception) {
            "Ошибка даты"
        }
    } else {
        return "Отсутствует"
    }
}

@Composable
private fun FullScreenLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
