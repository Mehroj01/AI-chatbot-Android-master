package com.chat.aichatbot.models

data class Message(
    val max_tokens:Int,
    val model: String,
    val prompt: String,
    val temperature: Int
)