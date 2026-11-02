package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisAiService
import com.example.data.ChatMessageEntity
import com.example.data.CreationEntity
import com.example.data.JarvisDatabase
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "JarvisViewModel"

enum class AppTab {
    CHAT,
    VOICE_CALL,
    VISION_SOLVER,
    STUDIO,
    HISTORY
}

enum class StudioMode {
    ESSAY,
    STORY,
    CODING,
    IMAGE_PROMPT
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application)
    private val dao = db.jarvisDao()
    private val aiService = JarvisAiService()
    val voiceManager = JarvisVoiceManager(application)
    private val firebaseDbService = com.example.data.FirebaseDatabaseService()

    val isCloudConnected: StateFlow<Boolean> = firebaseDbService.isCloudConnected

    private val prefs = application.getSharedPreferences("jarvis_user_prefs", android.content.Context.MODE_PRIVATE)

    // User Profile Avatar ("we can change our image")
    private val _userAvatarBitmap = MutableStateFlow<Bitmap?>(null)
    val userAvatarBitmap: StateFlow<Bitmap?> = _userAvatarBitmap.asStateFlow()

    // Answer in Voice (Auto-speak answers out loud softly)
    private val _isAutoVoiceEnabled = MutableStateFlow(prefs.getBoolean("auto_voice_enabled", true))
    val isAutoVoiceEnabled: StateFlow<Boolean> = _isAutoVoiceEnabled.asStateFlow()

    init {
        loadSavedUserAvatar()
    }

    private fun loadSavedUserAvatar() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "user_avatar.png")
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                _userAvatarBitmap.value = bitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load saved user avatar: ${e.message}")
        }
    }

    fun setUserAvatar(bitmap: Bitmap) {
        _userAvatarBitmap.value = bitmap
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(getApplication<Application>().filesDir, "user_avatar.png")
                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user avatar", e)
            }
        }
    }

    fun resetUserAvatar() {
        _userAvatarBitmap.value = null
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(getApplication<Application>().filesDir, "user_avatar.png")
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user avatar", e)
            }
        }
    }

    fun toggleAutoVoice() {
        val newState = !_isAutoVoiceEnabled.value
        _isAutoVoiceEnabled.value = newState
        prefs.edit().putBoolean("auto_voice_enabled", newState).apply()
    }

    // Current active navigation tab
    private val _currentTab = MutableStateFlow(AppTab.CHAT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Persistent Chat Messages from Room
    val chatMessages: StateFlow<List<ChatMessageEntity>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat text input & image selection
    private val _chatInputText = MutableStateFlow("")
    val chatInputText: StateFlow<String> = _chatInputText.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Voice Call State
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _callStatusText = MutableStateFlow("Stark Protocol Standby")
    val callStatusText: StateFlow<String> = _callStatusText.asStateFlow()

    private val _callTranscripts = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val callTranscripts: StateFlow<List<Pair<String, String>>> = _callTranscripts.asStateFlow()

    private var callTimerJob: Job? = null

    // Vision Solver State
    private val _visionBitmap = MutableStateFlow<Bitmap?>(null)
    val visionBitmap: StateFlow<Bitmap?> = _visionBitmap.asStateFlow()

    private val _visionPrompt = MutableStateFlow("Analyze this image thoroughly, solve any question, math formula, or code displayed, and explain step-by-step.")
    val visionPrompt: StateFlow<String> = _visionPrompt.asStateFlow()

    private val _visionResult = MutableStateFlow("")
    val visionResult: StateFlow<String> = _visionResult.asStateFlow()

    private val _isVisionAnalyzing = MutableStateFlow(false)
    val isVisionAnalyzing: StateFlow<Boolean> = _isVisionAnalyzing.asStateFlow()

    // Studio State (Essay, Story, Coding, Image Prompts)
    private val _studioMode = MutableStateFlow(StudioMode.ESSAY)
    val studioMode: StateFlow<StudioMode> = _studioMode.asStateFlow()

    private val _studioTopic = MutableStateFlow("")
    val studioTopic: StateFlow<String> = _studioTopic.asStateFlow()

    private val _studioDetails = MutableStateFlow("")
    val studioDetails: StateFlow<String> = _studioDetails.asStateFlow()

    private val _studioLanguage = MutableStateFlow("Python")
    val studioLanguage: StateFlow<String> = _studioLanguage.asStateFlow()

    private val _studioTone = MutableStateFlow("Analytical & Visionary")
    val studioTone: StateFlow<String> = _studioTone.asStateFlow()

    private val _studioResult = MutableStateFlow("")
    val studioResult: StateFlow<String> = _studioResult.asStateFlow()

    private val _isStudioGenerating = MutableStateFlow(false)
    val isStudioGenerating: StateFlow<Boolean> = _isStudioGenerating.asStateFlow()

    // Saved creations from Room
    val savedCreations: StateFlow<List<CreationEntity>> = dao.getAllCreations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateChatInput(text: String) {
        _chatInputText.value = text
    }

    fun selectImage(bitmap: Bitmap?) {
        _selectedImageBitmap.value = bitmap
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Send chat query (with optional attached image for multimodal analysis)
     */
    fun sendChatMessage() {
        val text = _chatInputText.value.trim()
        val bitmap = _selectedImageBitmap.value

        if (text.isEmpty() && bitmap == null) return

        val userPrompt = if (text.isEmpty()) "Please analyze this image and explain in detail." else text
        _chatInputText.value = ""
        _selectedImageBitmap.value = null
        _errorMessage.value = null
        _isGenerating.value = true
        _streamingResponse.value = ""

        viewModelScope.launch {
            try {
                // 1. Record user message in DB & sync to Firebase Realtime Database
                val userMsg = ChatMessageEntity(
                    role = "user",
                    text = userPrompt,
                    mode = if (bitmap != null) "vision" else "chat"
                )
                val userMsgId = dao.insertMessage(userMsg)
                firebaseDbService.syncChatMessage(userMsg.copy(id = userMsgId))

                val fullResponse = StringBuilder()
                if (bitmap != null) {
                    aiService.streamVisionAnalysis(bitmap, userPrompt).collect { chunk ->
                        fullResponse.append(chunk)
                        _streamingResponse.value = fullResponse.toString()
                    }
                } else {
                    aiService.streamChat(userPrompt).collect { chunk ->
                        fullResponse.append(chunk)
                        _streamingResponse.value = fullResponse.toString()
                    }
                }

                val finalOutput = fullResponse.toString()
                if (finalOutput.isNotBlank()) {
                    val jarvisMsg = ChatMessageEntity(
                        role = "jarvis",
                        text = finalOutput,
                        mode = if (bitmap != null) "vision" else "chat"
                    )
                    val jarvisMsgId = dao.insertMessage(jarvisMsg)
                    firebaseDbService.syncChatMessage(jarvisMsg.copy(id = jarvisMsgId))

                    // Auto-voice: answer in voice softly and politely if enabled
                    if (_isAutoVoiceEnabled.value) {
                        speakText(finalOutput)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat sending failed", e)
                _errorMessage.value = e.localizedMessage ?: "JARVIS neural uplink error. Please retry."
            } finally {
                _isGenerating.value = false
                _streamingResponse.value = ""
            }
        }
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun toggleVoiceListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening(
                onResult = { spoken ->
                    _chatInputText.value = spoken
                },
                onError = { err ->
                    _errorMessage.value = err
                }
            )
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearMessages()
        }
    }

    // ==========================================
    // VOICE CALL WITH JARVIS MODE
    // ==========================================

    fun startVoiceCall() {
        _isCallActive.value = true
        _callDurationSeconds.value = 0
        _callStatusText.value = "Neural Connection Established"
        _callTranscripts.value = listOf("JARVIS" to "Good day, sir. JARVIS online. Systems at peak capacity. What would you like to discuss?")

        // Start call duration timer
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_isCallActive.value) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }

        // Jarvis introductory greeting read aloud
        voiceManager.speak("Good day, sir. JARVIS online. Systems at peak capacity. How may I assist you today?") {
            // Once initial greeting finished, prompt user or listen
            if (_isCallActive.value && !_isCallMuted.value) {
                _callStatusText.value = "Listening to you, sir..."
                listenForUserInCall()
            }
        }
    }

    fun endVoiceCall() {
        _isCallActive.value = false
        callTimerJob?.cancel()
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
        _callStatusText.value = "Call Terminated"
    }

    fun toggleCallMute() {
        val newMuted = !_isCallMuted.value
        _isCallMuted.value = newMuted
        if (newMuted) {
            voiceManager.stopListening()
            _callStatusText.value = "Microphone Muted"
        } else {
            _callStatusText.value = "Listening to you, sir..."
            listenForUserInCall()
        }
    }

    private fun listenForUserInCall() {
        if (!_isCallActive.value || _isCallMuted.value) return
        voiceManager.startListening(
            onResult = { userSpoken ->
                handleUserCallSpeech(userSpoken)
            },
            onError = { _ ->
                if (_isCallActive.value && !_isCallMuted.value) {
                    // Retry listening gracefully during active call
                    viewModelScope.launch {
                        delay(1200)
                        if (_isCallActive.value && !_isCallMuted.value) {
                            listenForUserInCall()
                        }
                    }
                }
            }
        )
    }

    fun handleUserCallSpeech(userText: String) {
        if (userText.isBlank()) return
        val updated = _callTranscripts.value.toMutableList()
        updated.add("User" to userText)
        _callTranscripts.value = updated

        _callStatusText.value = "JARVIS Processing..."

        viewModelScope.launch {
            try {
                val contextString = _callTranscripts.value.takeLast(6).joinToString("\n") { "${it.first}: ${it.second}" }
                val reply = aiService.generateVoiceCallResponse(userText, contextString)

                val transcriptsAfterAi = _callTranscripts.value.toMutableList()
                transcriptsAfterAi.add("JARVIS" to reply)
                _callTranscripts.value = transcriptsAfterAi

                _callStatusText.value = "JARVIS Speaking..."
                voiceManager.speak(reply) {
                    if (_isCallActive.value && !_isCallMuted.value) {
                        _callStatusText.value = "Listening to you, sir..."
                        listenForUserInCall()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Call processing error", e)
                val fallback = "Forgive me, sir, there was a momentary disruption in the neural relay."
                voiceManager.speak(fallback) {
                    if (_isCallActive.value && !_isCallMuted.value) {
                        listenForUserInCall()
                    }
                }
            }
        }
    }

    // ==========================================
    // VISION SOLVER
    // ==========================================

    fun setVisionBitmap(bitmap: Bitmap?) {
        _visionBitmap.value = bitmap
        _visionResult.value = ""
    }

    fun setVisionPrompt(prompt: String) {
        _visionPrompt.value = prompt
    }

    fun solveVisionQuestion() {
        val bitmap = _visionBitmap.value ?: return
        val prompt = _visionPrompt.value

        _isVisionAnalyzing.value = true
        _visionResult.value = ""

        viewModelScope.launch {
            try {
                val fullText = StringBuilder()
                aiService.streamVisionAnalysis(bitmap, prompt).collect { chunk ->
                    fullText.append(chunk)
                    _visionResult.value = fullText.toString()
                }

                // Save to local creations & sync to Firebase DB
                val creation = CreationEntity(
                    type = "vision",
                    title = "Vision Solution: ${prompt.take(30)}...",
                    prompt = prompt,
                    content = fullText.toString()
                )
                val creationId = dao.insertCreation(creation)
                firebaseDbService.syncCreation(creation.copy(id = creationId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Vision solver failed", e)
                _visionResult.value = "Failed to solve: ${e.localizedMessage}"
            } finally {
                _isVisionAnalyzing.value = false
            }
        }
    }

    // ==========================================
    // CREATION STUDIO (Essays, Stories, Coding, Image Prompts)
    // ==========================================

    fun setStudioMode(mode: StudioMode) {
        _studioMode.value = mode
        _studioResult.value = ""
    }

    fun updateStudioTopic(topic: String) {
        _studioTopic.value = topic
    }

    fun updateStudioDetails(details: String) {
        _studioDetails.value = details
    }

    fun updateStudioLanguage(lang: String) {
        _studioLanguage.value = lang
    }

    fun updateStudioTone(tone: String) {
        _studioTone.value = tone
    }

    fun generateStudioCreation() {
        val topic = _studioTopic.value.trim()
        if (topic.isEmpty()) return

        _isStudioGenerating.value = true
        _studioResult.value = ""

        val prompt = when (_studioMode.value) {
            StudioMode.ESSAY -> """
                [JARVIS ESSAY ARCHITECT]
                Topic: $topic
                Tone & Style: ${_studioTone.value}
                Additional Specifications: ${_studioDetails.value.ifBlank { "Comprehensive, well-structured, persuasive essay" }}

                Generate a complete, masterfully written essay including:
                1. Compelling Title
                2. Engaging Introduction with a definitive Thesis Statement
                3. Well-evidenced Body Paragraphs with analytical depth
                4. Counter-arguments & synthesis
                5. Resonant Conclusion
            """.trimIndent()

            StudioMode.STORY -> """
                [JARVIS NARRATIVE ENGINE]
                Story Concept / Premise: $topic
                Genre & Atmosphere: ${_studioTone.value}
                Characters & Plot Elements: ${_studioDetails.value.ifBlank { "Dynamic plot, vivid sensory descriptions, unexpected twist" }}

                Write an enthralling, cinematic short story with rich dialogue, immersive world-building, and strong pacing.
            """.trimIndent()

            StudioMode.CODING -> """
                [JARVIS CODE FOUNDRY]
                Language / Framework: ${_studioLanguage.value}
                Task Description: $topic
                Detailed Requirements: ${_studioDetails.value.ifBlank { "Clean architecture, error handling, performance optimization" }}

                Provide:
                1. Architecture & Design explanation
                2. Complete, executable, bug-free code wrapped in ```${_studioLanguage.value.lowercase()} code fences
                3. Step-by-step walkthrough of key logic
                4. Testing or usage examples
            """.trimIndent()

            StudioMode.IMAGE_PROMPT -> """
                [JARVIS VISUAL SYNTHESIZER]
                Subject / Art Vision: $topic
                Aesthetic Style: ${_studioTone.value}
                Visual Notes: ${_studioDetails.value.ifBlank { "Hyper-detailed, octane render, volumetric cinematic lighting" }}

                Provide:
                1. Primary High-Fidelity Prompt (optimized for Midjourney / Stable Diffusion / Gemini Imagen)
                2. Cinematic Camera, Lens (e.g. 85mm f/1.4), Color Palette, Lighting Breakdown
                3. Negative Prompts / Style Keywords to avoid artifacts
                4. Creative Variations (Cyberpunk, Realistic, Anime, Watercolor)
            """.trimIndent()
        }

        viewModelScope.launch {
            try {
                val fullOutput = StringBuilder()
                aiService.streamChat(prompt).collect { chunk ->
                    fullOutput.append(chunk)
                    _studioResult.value = fullOutput.toString()
                }

                // Automatically save to Room creations
                val title = when (_studioMode.value) {
                    StudioMode.ESSAY -> "Essay: $topic"
                    StudioMode.STORY -> "Story: $topic"
                    StudioMode.CODING -> "Code (${_studioLanguage.value}): $topic"
                    StudioMode.IMAGE_PROMPT -> "Visual Concept: $topic"
                }

                val creation = CreationEntity(
                    type = _studioMode.value.name.lowercase(),
                    title = title.take(60),
                    prompt = topic,
                    content = fullOutput.toString(),
                    language = if (_studioMode.value == StudioMode.CODING) _studioLanguage.value else null
                )
                val creationId = dao.insertCreation(creation)
                firebaseDbService.syncCreation(creation.copy(id = creationId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Studio generation failed", e)
                _studioResult.value = "Generation failed: ${e.localizedMessage}"
            } finally {
                _isStudioGenerating.value = false
            }
        }
    }

    fun deleteCreation(id: Long) {
        viewModelScope.launch {
            dao.deleteCreation(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        voiceManager.release()
    }
}
