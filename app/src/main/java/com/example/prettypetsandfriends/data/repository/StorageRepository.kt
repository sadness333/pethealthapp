package com.example.prettypetsandfriends.data.repository

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = Firebase.storage.reference

    suspend fun uploadPetImage(userId: String, fileUri: Uri): String {
        return try {
            val imageRef = storage.child("pet_images/$userId/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(fileUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки изображения: ${e.message}")
        }
    }

    suspend fun uploadUserImage(userId: String, fileUri: Uri): String {
        return try {
            val imageRef = storage.child("users_avatars/$userId/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(fileUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки изображения: ${e.message}")
        }
    }
}