package com.example.prettypetsandfriends.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.R
import com.example.prettypetsandfriends.utils.LocalPetState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun AuthScreen(navController: NavController) {
    val petState = LocalPetState.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task, navController, context, scope)
        } else {
            errorMessage = "Авторизация через Google отменена"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pets_black),
                contentDescription = "Логотип приложения",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Добро пожаловать!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Пароль"
                            )
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator()
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    Firebase.auth.signInWithEmailAndPassword(email, password).await()
                                    petState.loadPets(Firebase.auth.currentUser!!.uid)
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = when (e) {
                                        is FirebaseAuthInvalidUserException -> "Пользователь не найден"
                                        is FirebaseAuthInvalidCredentialsException -> "Неверный пароль"
                                        else -> "Ошибка входа: Пустой ввод"
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Войти",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    TextButton(
                        onClick = { navController.navigate("registration") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Ещё нет аккаунта? Зарегистрироваться",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            "или",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { scope.launch {
                            try {
                                val signInIntent = googleSignInClient.signInIntent
                                googleAuthLauncher.launch(signInIntent)
                            } catch (e: Exception) {
                                errorMessage = "Ошибка запуска Google Sign-In: ${e.localizedMessage}"
                            }
                        } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.google),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Продолжить с Google",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleGoogleSignInResult(
    task: Task<GoogleSignInAccount>,
    navController: NavController,
    context: Context,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = Firebase.auth.signInWithCredential(credential).await()
            val user = authResult.user

            val userRef = Firebase.database.reference
                .child("users")
                .child(user!!.uid)

            val snapshot = userRef.get().await()

            if (!snapshot.exists()) {
                val userData = hashMapOf(
                    "email" to user.email,
                    "name" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString(),
                    "createdAt" to ServerValue.TIMESTAMP,
                    "provider" to "google"
                )

                userRef.setValue(userData).await()
            }

            navController.navigate("main") {
                popUpTo("auth") { inclusive = true }
            }
        } catch (e: Exception) {
            val error = when (e) {
                is ApiException -> when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> "Авторизация отменена"
                    CommonStatusCodes.TIMEOUT -> "Таймаут подключения"
                    else -> "Ошибка Google: ${e.statusCode}"
                }
                is CancellationException -> "Операция отменена"
                else -> "Ошибка авторизации: ${e.localizedMessage}"
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}