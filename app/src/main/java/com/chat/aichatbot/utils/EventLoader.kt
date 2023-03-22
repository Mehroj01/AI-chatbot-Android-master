package com.chat.aichatbot.utils

import com.chat.aichatbot.models.ChatResponse

sealed class EventLoader {
    object Loading : EventLoader()
    data class Success(var data: ChatResponse) : EventLoader()
    data class Error(var error: String) : EventLoader()
}
