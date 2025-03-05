package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entities.UserProfile
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.example.prettypetsandfriends.ui.components.ThemeSelectionDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun UserMenuScreen(navController: NavController) {
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var currentTheme by remember { mutableStateOf(AppTheme.SYSTEM) }
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val coroutineScope = rememberCoroutineScope()

    val user = remember {
        UserProfile(
            name = "Иван Петров",
            email = "ivan.petrov@example.com",
            avatarRes = R.drawable.ic_person_black
        )
    }

    LaunchedEffect(Unit) {
        ThemeManager.getThemeFlow(context).collect { theme ->
            currentTheme = theme
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { newTheme ->
                currentTheme = newTheme
                coroutineScope.launch {
                    ThemeManager.saveTheme(context, newTheme)
                }
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Профиль",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileCard(
                user = user,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuCard(
                    title = "Редактирование профиля",
                    icon = Icons.Default.Edit,
                    onClick = { navController.navigate("edit_profile") }
                )

                MenuCard(
                    title = "Уведомления",
                    icon = Icons.Default.Notifications,
                    onClick = { navController.navigate("settings") }
                )

                MenuCard(
                    title = "Смена темы",
                    icon = Icons.Rounded.Refresh,
                    onClick = {  showThemeDialog = true }
                )

                MenuCard(
                    title = "Удалить аккаунт",
                    icon = Icons.Default.Delete,
                    onClick = { /* Логика удаления */ },
                    isDestructive = true
                )

                MenuCard(
                    title = "Выйти из аккаунта",
                    icon = Icons.Default.ExitToApp,
                    onClick = {
                        Firebase.auth.signOut()
                        googleSignInClient.signOut().addOnCompleteListener {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    },
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = user.avatarRes),
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val cardColor = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}