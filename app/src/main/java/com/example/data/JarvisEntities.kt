package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "jarvis", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val mode: String = "chat" // "chat", "call", "vision"
)

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "essay", "story", "code", "image_prompt"
    val title: String,
    val prompt: String,
    val content: String,
    val language: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
