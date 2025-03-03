package com.example.prettypetsandfriends.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entities.PetProfile

@Composable
fun CustomTopBar(
    navController: NavController,
    showPetDropdown: Boolean = false,
    onPetClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    pets: List<PetProfile> = emptyList(),
    name: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val statusBarHeightDp: Dp = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeightDp)
                .padding(horizontal = 16.dp)
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            else {
                Box(modifier = Modifier.size(32.dp).clickable { onPetClick() }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pets_black),
                        contentDescription = "Питомец",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onPrimaryContainer,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
            )

            if (!showBackButton) {
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_black),
                        contentDescription = "Профиль",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }

    if (!showBackButton && showPetDropdown) {
        PetDropdownMenu(showPetDropdown, onDismiss, pets, navController)
    }
}

@Composable
fun PetDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    pets: List<PetProfile>,
    navController: NavController
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        pets.forEach { pet ->
            DropdownMenuItem(
                text = { Text(pet.name) },
                onClick = {
                    navController.navigate("pet_profile/${pet.id}")
                    onDismiss()
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Добавить питомца") },
            onClick = {
                navController.navigate("add_pet")
                onDismiss()
            }
        )
    }
}