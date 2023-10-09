package com.chat.aichatbot.models

data class RequestBody(
    val messages: List<MessageX>,
    val model: String
)