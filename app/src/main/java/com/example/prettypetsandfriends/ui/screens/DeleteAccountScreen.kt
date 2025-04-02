package com.example.prettypetsandfriends.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.utils.LocalPetState
import com.example.prettypetsandfriends.backend.PetState
import com.example.prettypetsandfriends.backend.repository.PetRepository
import com.example.prettypetsandfriends.backend.repository.UserRepository
import com.example.prettypetsandfriends.ui.components.CustomTopBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DeleteAccountScreen(navController: NavController) {
    val context = LocalContext.current
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val userRepository = remember { UserRepository() }
    val petRepository = remember { PetRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val petState = LocalPetState.current
    val googleSignInClient = GoogleSignIn.getClient(context, gso)



    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Удаление аккаунта",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            WarningCard()

            Spacer(modifier = Modifier.height(40.dp))

            DeleteAccountButton(
                onClick = { showConfirmationDialog = true }
            )

            if (showConfirmationDialog) {
                ConfirmationDialog(
                    onConfirm = {
                        CoroutineScope(Dispatchers.IO).launch {

                            deleteUserAccount(
                                googleSignInClient = googleSignInClient,
                                petState = petState,
                                currentUser = currentUser,
                                userRepository = userRepository,
                                petRepository = petRepository,
                                navController = navController
                            )
                        }
                        showConfirmationDialog = false
                    },
                    onDismiss = { showConfirmationDialog = false }
                )
            }
        }
    }
}

@Composable
private fun WarningCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Внимание! Это действие необратимо",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Все ваши данные будут безвозвратно удалены:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = "• Профиль пользователя\n• Все питомцы\n• История и документы",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DeleteAccountButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    ) {
        Text(
            text = "Удалить аккаунт навсегда",
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Последнее предупреждение")
        },
        text = {
            Text("Вы уверены, что хотите полностью удалить свой аккаунт и все данные?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Подтвердить удаление")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private suspend fun deleteUserAccount(
    googleSignInClient: GoogleSignInClient,
    petState: PetState,
    currentUser: FirebaseUser?,
    userRepository: UserRepository,
    petRepository: PetRepository,
    navController: NavController
) {


    if (currentUser == null) return

    petRepository.deleteAllPets(currentUser.uid)

    userRepository.deleteUserData(currentUser.uid)

    currentUser.delete()
        .addOnCompleteListener {
            petState.selectedPet = null
            Firebase.auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
}
