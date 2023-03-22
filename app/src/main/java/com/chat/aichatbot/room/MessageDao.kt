package com.chat.aichatbot.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chat.aichatbot.models.MessageModule

@androidx.room.Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: MessageModule)

    @Delete
    fun deleteMessage(message: MessageModule)


    @Query("SELECT * FROM messageModule")
    fun getAllMessages(): List<MessageModule>

    @Query("SELECT * FROM messageModule WHERE chatId = :chatId")
    fun getThreadMessages(chatId: Int): List<MessageModule>

    @Query("SELECT * FROM messageModule WHERE chatId = :chatId AND isUser = :isUser")
    fun getThreadUserMessages(chatId: Int, isUser: Boolean): List<MessageModule>

    @Query("DELETE FROM messageModule WHERE chatId = :chatId")
    fun deleteThreadMessages(chatId: Int)

    @Query("DELETE FROM messageModule")
    fun deleteAllMessage()

    @Delete
    fun deleteAIMessage(aiMessage: MessageModule)
}