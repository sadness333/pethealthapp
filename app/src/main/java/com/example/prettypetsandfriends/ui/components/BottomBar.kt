package com.example.prettypetsandfriends.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entites.MenuItem


@Composable
fun CustomBottomNavigation(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val items = listOf(
        MenuItem("Главная", painterResource(id = R.drawable.ic_home_black), "main"),
        MenuItem("Дневник", painterResource(id = R.drawable.ic_book_black), "health_diary"),
        MenuItem("Питание", painterResource(id = R.drawable.ic_eating), "nutrition"),
        MenuItem("Чат", painterResource(id = R.drawable.ic_chat_black), "ai_assistant"),
        MenuItem("События", painterResource(id = R.drawable.ic_calendar_black), "calendar")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}