package com.example.prettypetsandfriends.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.backend.LocalPetState
import com.example.prettypetsandfriends.data.entities.PetProfile

@Composable
fun CustomTopBar(
    navController: NavController,
    showPetDropdown: Boolean = false,
    onPetClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    name: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val petState = LocalPetState.current
    val density = LocalDensity.current
    var buttonOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

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
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
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
            } else {
                Box {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onPetClick() }
                            .onGloballyPositioned { coordinates ->
                                buttonOffset = DpOffset(
                                    x = with(density) { coordinates.positionInRoot().x.toDp() - 25.dp},
                                    y = with(density) { (coordinates.positionInRoot().y + coordinates.size.height).toDp() - 75.dp}
                                )
                            }
                    ) {
                        Image(
                            painter = painterResource(id = petState.selectedPet?.photoRes ?: R.drawable.ic_pets_black),
                            contentDescription = "Питомец",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    if (showPetDropdown) {
                        PetDropdownMenu(
                            expanded = showPetDropdown,
                            onDismiss = onDismiss,
                            pets = petState.allPets,
                            navController = navController,
                            offset = buttonOffset
                        )
                    }
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
}

@Composable
fun PetDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    pets: List<PetProfile>,
    navController: NavController,
    offset: DpOffset
) {
    val petState = LocalPetState.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onPrimary)
            .width(140.dp)
    ) {
        Box(modifier = Modifier.requiredHeight(150.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                pets.forEach { pet ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = pet.photoRes ?: R.drawable.ic_pets_black),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            petState.selectPet(pet)
                            onDismiss()
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить питомца")
                        }
                    },
                    onClick = {
                        navController.navigate("add_pet")
                        onDismiss()
                    }
                )
            }
        }
    }
}