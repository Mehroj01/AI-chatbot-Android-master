package com.chat.aichatbot.room

import androidx.room.*


@Dao
interface ThreadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(thread: ThreadModule)

    @Delete
    fun deleteThread(thread: ThreadModule)


    @Query("SELECT * FROM threads")
    fun getAllThreads(): List<ThreadModule>

    @Query("DELETE FROM threads")
    fun deleteAllThreads()
}