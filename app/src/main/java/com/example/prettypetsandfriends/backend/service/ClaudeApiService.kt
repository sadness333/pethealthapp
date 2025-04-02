package com.example.prettypetsandfriends.backend.service

import com.example.prettypetsandfriends.data.entities.ai.ClaudeRequest
import com.example.prettypetsandfriends.data.entities.ai.ClaudeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("anthropic/eu/v1/messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): Response<ClaudeResponse>
}
