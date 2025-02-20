package com.example.prettypetsandfriends.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.entites.PetProfile

@Composable
fun CustomTopBar(
    navController: NavController,
    showPetDropdown: Boolean,
    onPetClick: () -> Unit,
    onDismiss: () -> Unit,
    pets: List<PetProfile>,
    name: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
                .height(80.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(48.dp).clickable { onPetClick() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pets_black),
                    contentDescription = "Питомец",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
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

            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person_black),
                    contentDescription = "Профиль",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    PetDropdownMenu(showPetDropdown, onDismiss, pets, navController)
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