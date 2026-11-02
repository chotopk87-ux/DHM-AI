package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessageEntity::class, CreationEntity::class], version = 1, exportSchema = false)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_ai_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
