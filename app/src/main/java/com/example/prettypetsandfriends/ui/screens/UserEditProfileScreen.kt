package com.example.prettypetsandfriends.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.repository.PetRepository
import com.example.prettypetsandfriends.data.repository.StorageRepository
import com.example.prettypetsandfriends.data.repository.UserRepository
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UserEditProfileScreen(navController: NavController) {
    val userRepository = remember { UserRepository() }
    val storageRepo = remember { StorageRepository() }
    val petRepository = remember { PetRepository() }
    val currentUser = petRepository.getCurrentUser()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    LaunchedEffect(Unit) {
        userRepository.observeUserData().collect { user ->
            name = user.name ?: ""
            email = user.email ?: ""
            phone = user.phone ?: ""
            bio = user.bio ?: ""
            selectedImageUri = if (!user.photoUrl.isNullOrEmpty()) {
                Uri.parse(user.photoUrl)
            } else {
                null
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Редактирование",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            PhotoSection(
                selectedImageUri = selectedImageUri,
                onPickImage = {
                    imagePicker.launch("image/*")
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("О себе") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (currentUser == null) return@Button

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val photoUrl = selectedImageUri?.let { uri ->
                                storageRepo.uploadUserImage(
                                    userId = currentUser.uid,
                                    fileUri = uri
                                )
                            }

                            userRepository.updateUserProfile(
                                name = name,
                                email = email,
                                phone = phone,
                                bio = bio,
                                photoUrl = photoUrl
                            )
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Сохранить изменения")
            }
        }
    }
}
