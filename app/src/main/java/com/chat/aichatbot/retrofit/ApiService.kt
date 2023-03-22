package com.chat.aichatbot.retrofit

import com.chat.aichatbot.models.ChatResponse
import com.chat.aichatbot.models.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/v1/completions")
    suspend fun getChatResponse(
        @Header("Authorization") token: String,
        @Body message: Message
    ): Response<ChatResponse>
}