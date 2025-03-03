package com.example.prettypetsandfriends.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entities.MenuItem


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
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp))
                },
                label = {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
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
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}