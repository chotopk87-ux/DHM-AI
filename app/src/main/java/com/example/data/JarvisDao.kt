package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()

    @Query("SELECT * FROM creations ORDER BY timestamp DESC")
    fun getAllCreations(): Flow<List<CreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: CreationEntity): Long

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteCreation(id: Long)

    @Query("DELETE FROM creations")
    suspend fun clearCreations()
}
