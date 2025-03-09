package com.example.prettypetsandfriends.data.repository

import android.net.Uri
import com.example.prettypetsandfriends.data.entities.Document
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = Firebase.storage.reference
    private val documentsRef get() = storage.child("documents")

    suspend fun uploadDocument(
        userId: String,
        petId: String,
        fileUri: Uri,
        title: String
    ): Document {
        try {
            val fileRef = documentsRef.child("$userId/$petId/${UUID.randomUUID()}")
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata("title", title)
                .setCustomMetadata("petId", petId)
                .build()

            val uploadTask = fileRef.putFile(fileUri, metadata).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()

            return Document(
                id = fileRef.name,
                petId = petId,
                title = title,
                url = downloadUrl,
                uploadDate = uploadTask.metadata?.updatedTimeMillis ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки документа: ${e.message}")
        }
    }

    suspend fun getPetDocuments(userId: String, petId: String): List<Document> {
        return try {
            val listResult = documentsRef.child("$userId/$petId").listAll().await()
            listResult.items.mapNotNull { item ->
                try {
                    val metadata = item.metadata.await()
                    Document(
                        id = item.name,
                        petId = metadata.getCustomMetadata("petId") ?: "",
                        title = metadata.getCustomMetadata("title") ?: "Без названия",
                        url = item.downloadUrl.await().toString(),
                        uploadDate = metadata.updatedTimeMillis
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.uploadDate }
        } catch (e: Exception) {
            throw Exception("Ошибка получения документов: ${e.message}")
        }
    }

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