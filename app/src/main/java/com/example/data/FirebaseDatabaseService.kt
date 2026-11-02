package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "FirebaseDbService"

class FirebaseDatabaseService {

    private val db by lazy {
        try {
            Firebase.database("https://jarvis-ai-18b09-default-rtdb.firebaseio.com").apply {
                try {
                    setPersistenceEnabled(true)
                } catch (e: Exception) {
                    // Persistence can only be set once
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Firebase Realtime Database: ${e.message}")
            Firebase.database
        }
    }

    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    init {
        try {
            val connectedRef = db.getReference(".info/connected")
            connectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    _isCloudConnected.value = connected
                    Log.d(TAG, "Firebase Realtime Database connected state: $connected")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Database listener cancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Error attaching connection listener: ${e.message}")
        }
    }

    /**
     * Uploads or syncs a chat message to Firebase Realtime Database
     */
    fun syncChatMessage(message: ChatMessageEntity) {
        try {
            val msgRef = db.getReference("chat_messages").child(message.id.toString())
            val map = mapOf(
                "id" to message.id,
                "role" to message.role,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "mode" to message.mode
            )
            msgRef.setValue(map).addOnSuccessListener {
                Log.d(TAG, "Chat message ${message.id} synced to Firebase DB.")
            }.addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync message ${message.id}: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Database write exception: ${e.message}")
        }
    }

    /**
     * Uploads or syncs a studio creation (essay, story, code) to Firebase Realtime Database
     */
    fun syncCreation(creation: CreationEntity) {
        try {
            val creationRef = db.getReference("creations").child(creation.id.toString())
            val map = mapOf(
                "id" to creation.id,
                "type" to creation.type,
                "title" to creation.title,
                "prompt" to creation.prompt,
                "content" to creation.content,
                "language" to (creation.language ?: ""),
                "timestamp" to creation.timestamp
            )
            creationRef.setValue(map).addOnSuccessListener {
                Log.d(TAG, "Creation ${creation.id} synced to Firebase DB.")
            }.addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync creation ${creation.id}: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Database creation write exception: ${e.message}")
        }
    }
}
