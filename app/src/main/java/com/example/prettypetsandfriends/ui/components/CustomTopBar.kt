package com.example.prettypetsandfriends.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.backend.repository.UserRepository

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
    val user by UserRepository().observeUserData().collectAsState(initial = null)
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
                        AsyncImage(
                            model = petState.selectedPet?.photoUrl ?: R.drawable.ic_pets_black,
                            contentDescription = "Фото питомца",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            placeholder = painterResource(id = R.drawable.ic_pets_black),
                            error = painterResource(id = R.drawable.ic_pets_black)
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
                IconButton(onClick = { navController.navigate("profile") },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)) {
                    AsyncImage(
                        model = user?.photoUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Профиль",
                        contentScale = ContentScale.Crop
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
    pets: List<Pet>,
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
        Box(modifier = Modifier.heightIn(min = 30.dp, max=150.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                pets.forEach { pet ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = pet.photoUrl,
                                    contentDescription = "Фото питомца",
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height(30.dp)
                                        .clip(CircleShape),
                                    placeholder = painterResource(id = R.drawable.ic_pets_black),
                                    error = painterResource(id = R.drawable.ic_pets_black)
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