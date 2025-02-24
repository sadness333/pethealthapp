package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar

@Composable
fun StatisticsScreen(navController: NavController) {

    val statistics = remember {
        mapOf(
            "Визиты к ветеринару" to "5",
            "Потрачено на корм" to "1500 руб.",
            "Потрачено на лечение" to "5000 руб."
        )
    }

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            statistics.forEach { (title, value) ->
                StatisticCard(title, value)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StatisticCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}