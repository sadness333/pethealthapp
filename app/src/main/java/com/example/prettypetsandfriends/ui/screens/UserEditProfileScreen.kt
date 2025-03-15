package com.example.prettypetsandfriends.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.repository.PetRepository
import com.example.prettypetsandfriends.data.repository.StorageRepository
import com.example.prettypetsandfriends.data.repository.UserRepository
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.example.prettypetsandfriends.ui.theme.Purple40
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UserEditProfileScreen(navController: NavController) {
    val user by UserRepository().observeUserData().collectAsState(initial = null)
    val userRepository = remember { UserRepository() }
    val storageRepo = remember { StorageRepository() }
    val petRepository = remember { PetRepository() }
    val currentUser = petRepository.getCurrentUser()
    val scope = rememberCoroutineScope()
    var isPick = false

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
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
            role = user.role ?: "user"
            selectedImageUri = user.photoUrl?.let { Uri.parse(it) }
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            PhotoSection(
                selectedImageUri = selectedImageUri,
                onPickImage = {
                    imagePicker.launch("image/*")
                    isPick = true
                }
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(6.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    listOf(
                        Triple("Имя", name, Icons.Filled.Person),
                        Triple("Email", email, Icons.Filled.Email),
                        Triple("Телефон", phone, Icons.Filled.Phone),
                        Triple("О себе", bio, Icons.Filled.Info)
                    ).forEachIndexed { index, (label, value, icon) ->
                        CustomOutlinedTextField(
                            value = value,
                            label = label,
                            icon = icon,
                            isMultiline = index == 3,
                            onValueChange = {
                                when(index) {
                                    0 -> name = it
                                    1 -> email = it
                                    2 -> phone = it
                                    3 -> bio = it
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    if (currentUser == null) return@Button
                    scope.launch {
                        try {
                            val photoUrl = if (isPick) {
                                storageRepo.uploadUserImage(
                                    userId = currentUser.uid,
                                    fileUri = selectedImageUri!!
                                )
                            } else {
                                user?.photoUrl
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
                            "Ошибка сохранения: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "Сохранить изменения", fontSize = 16.sp)
            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomOutlinedTextField(
    value: String,
    label: String,
    icon: ImageVector,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        ),
        singleLine = !isMultiline,
        maxLines = if (isMultiline) 3 else 1
    )
}