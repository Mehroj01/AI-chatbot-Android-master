package com.chat.aichatbot.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threads")
data class ThreadModule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
) : java.io.Serializable {


}