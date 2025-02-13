package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.ui.components.MenuItem
import kotlinx.coroutines.launch

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                // Заголовок меню
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pet Health Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val menuItems = listOf(
                    MenuItem("Главная",painterResource(id = R.drawable.ic_home_black) , "main"),
                    MenuItem("Дневник здоровья", painterResource(id = R.drawable.ic_book_black), "health_diary"),
                    MenuItem("Сообщество",painterResource(id = R.drawable.ic_people_black) , "community"),
                    MenuItem("AI-ассистент",painterResource(id = R.drawable.ic_chat_black) , "ai_assistant"),
                    MenuItem("Календарь",painterResource(id = R.drawable.ic_calendar_black) , "calendar"),
                    MenuItem("Профиль",painterResource(id = R.drawable.ic_account_box_black) , "profile"),
                    MenuItem("Настройки",painterResource(id = R.drawable.ic_settings_black) , "settings")
                )

                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // TODO: Переход на экран (item.route)
                        },
                        icon = {
                            Icon(
                                painter = item.icon,
                                contentDescription = item.title
                            )
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Moscow, RU") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Open profile */ }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_person_black),
                                contentDescription = "Аватар",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            PetCareScreen(paddingValues)
        }
    }
}

@Composable
fun PetCareScreen(paddingValues: PaddingValues) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Hi Jennifer,",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Let's take care of your cutie pets!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            // Services Row

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 24.dp,
                        top = 16.dp
                        ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Checkup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 09:00 AM • 14 July 2020",
                        color = Color.Gray
                    )
                }
            }

            // My Pets Section
            Text(
                text = "My Pets",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(listOf("Morphy", "Kitten")) { pet ->
                    PetCard(petName = pet)
                }
            }

            // Pet Care Card
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Helping you to take good care of your Pet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "All pets deserves some care and love!",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { /*TODO*/ }) {
                        Text("Get Started")
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(petName: String) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = petName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when(petName) {
                    "Morphy" -> "2 Years 3 Months"
                    else -> "3 Years 6 Months"
                },
                color = Color.Gray
            )
        }
    }
}