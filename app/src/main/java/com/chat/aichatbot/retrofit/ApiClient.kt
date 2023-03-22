package com.chat.aichatbot.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {
    private const val BASE_URL = "https://api.openai.com"

    private val httpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(21, TimeUnit.SECONDS)
        .writeTimeout(21, TimeUnit.SECONDS)
        .readTimeout(42, TimeUnit.SECONDS)

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL).client(httpClientBuilder.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}