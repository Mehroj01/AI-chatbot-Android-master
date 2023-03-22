package com.chat.aichatbot.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chat.aichatbot.models.MessageModule

@Database(
    entities = [MessageModule::class, ThreadModule::class],
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun threadDao(): ThreadDao

    companion object {
        private var appDatabase: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return appDatabase!!
        }
    }
}