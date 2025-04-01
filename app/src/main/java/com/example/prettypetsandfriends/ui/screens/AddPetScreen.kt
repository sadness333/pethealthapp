package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.data.repository.PetRepository
import com.example.prettypetsandfriends.data.repository.StorageRepository
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.prettypetsandfriends.data.entities.Pet
import com.example.prettypetsandfriends.data.entities.WeightHistory
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun AddPetScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PetRepository() }
    val storageRepo = remember { StorageRepository() }
    val currentUser = repository.getCurrentUser()

    // Состояния полей
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Кот") }
    var calories by remember { mutableStateOf("") }
    var vetNotes by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val petTypes = listOf("Кот", "Собака", "Грызун", "Птица", "Рыбка", "Другое")
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Добавить питомца",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { CustomBottomNavigation(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                PhotoSection(
                    selectedImageUri = selectedImageUri,
                    onPickImage = { imagePicker.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Основная информация")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя питомца*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                        label = { Text("Возраст*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (it.isValidDecimal()) weight = it },
                        label = { Text("Вес (кг)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                PetTypeDropdown(
                    selectedValue = petType,
                    options = petTypes,
                    onValueChange = { petType = it }
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Порода") },
                    modifier = Modifier.fillMaxWidth()
                )

                SectionTitle("Питание")
                OutlinedTextField(
                    value = calories,
                    onValueChange = { if (it.isValidDecimal()) calories = it },
                    label = { Text("Дневная норма ккал") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                SectionTitle("Здоровье")
                OutlinedTextField(
                    value = vetNotes,
                    onValueChange = { vetNotes = it },
                    label = { Text("Рекомендации ветеринара") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SaveButton(
                    isLoading = isLoading,
                    enabled = name.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty(),
                    onClick = {
                        if (currentUser == null) {
                            errorMessage = "Требуется авторизация"
                            return@SaveButton
                        }

                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            try {
                                val photoUrl = selectedImageUri?.let { uri ->
                                    storageRepo.uploadPetImage(
                                        userId = currentUser.uid,
                                        fileUri = uri
                                    )
                                }
                                val newPet = Pet(
                                    name = name,
                                    type = petType,
                                    breed = breed,
                                    age = age.toInt(),
                                    weight = weight.toDouble(),
                                    ownerId = currentUser.uid,
                                    photoUrl = photoUrl ?: "https://cdn-icons-png.flaticon.com/128/4225/4225935.png",
                                    nutrition = Pet.PetNutrition(
                                        dailyCalories = calories.toIntOrNull() ?: 0,
                                        vetRecommendations = vetNotes
                                    )
                                )

                                repository.addPet(newPet)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                errorMessage = when (e) {
                                    is NumberFormatException -> "Некорректные числовые значения"
                                    is StorageException -> "Ошибка загрузки фото: ${e.message}"
                                    else -> "Ошибка сохранения: ${e.localizedMessage}"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoSection(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .size(160.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPickImage() }
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_pets_black),
                        error = painterResource(R.drawable.ic_pets_black)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = "Добавить фото",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onPickImage,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "Изменить",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedImageUri != null) "Изменить фото" else "Добавить фото",
                style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SaveButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Сохранить", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PetTypeDropdown(
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text("Тип животного*") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun String.isValidDecimal(): Boolean {
    return matches(Regex("^\\d*\\.?\\d*$"))
}