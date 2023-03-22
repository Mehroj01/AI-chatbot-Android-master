package com.chat.aichatbot.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.chat.aichatbot.room.ThreadModule

@Entity
class MessageModule(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var chatId: Int,
    var isUser: Boolean, var message: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageModule

        if (id != other.id) return false
        if (chatId != other.chatId) return false
        if (isUser != other.isUser) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + chatId
        result = 31 * result + isUser.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }
}