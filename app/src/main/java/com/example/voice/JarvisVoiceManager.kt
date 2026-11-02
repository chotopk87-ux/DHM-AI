package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "JarvisVoiceManager"

class JarvisVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private var onSpeakingFinishedCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true

            // Set English UK as default locale for refined JARVIS butler tone
            val ukLocale = Locale.UK
            val result = tts?.setLanguage(ukLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }

            // Find and apply a soft, calm male voice
            try {
                val availableVoices = tts?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val maleVoice = availableVoices.firstOrNull { voice ->
                        voice.locale.language == "en" &&
                        (voice.name.contains("male", ignoreCase = true) ||
                         voice.name.contains("en-gb-x-rjs", ignoreCase = true) ||
                         voice.name.contains("en-gb-x-gba", ignoreCase = true) ||
                         voice.name.contains("en-gb-x-gbb", ignoreCase = true) ||
                         voice.name.contains("en-us-x-sfg", ignoreCase = true) ||
                         voice.name.contains("en-us-x-iol", ignoreCase = true))
                    } ?: availableVoices.firstOrNull { voice ->
                        voice.locale.language == "en" && voice.locale.country == "GB"
                    }

                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                        Log.d(TAG, "Selected soft male voice: ${maleVoice.name}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error choosing voice profile: ${e.message}")
            }

            // Soft, calm, gentle male pitch and measured speech rate
            tts?.setPitch(0.85f)     // Deeper, smooth male timbre (very soft & polite)
            tts?.setSpeechRate(0.90f) // Gentle, calm, unhurried polite cadence

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeakingFinishedCallback?.invoke()
                    onSpeakingFinishedCallback = null
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeakingFinishedCallback?.invoke()
                    onSpeakingFinishedCallback = null
                }
            })
            Log.d(TAG, "TTS Initialized with soft male voice profile.")
        } else {
            Log.e(TAG, "Failed to initialize TTS engine.")
        }
    }

    /**
     * Automatically detects language script to speak in the user's native tongue
     */
    private fun detectLanguageLocale(text: String): Locale {
        return when {
            text.any { it in '\u0900'..'\u097F' } -> Locale.forLanguageTag("hi-IN") // Hindi / Devanagari
            text.any { it in '\u0980'..'\u09FF' } -> Locale.forLanguageTag("bn-BD") // Bengali
            text.any { it in '\u0600'..'\u06FF' } -> Locale.forLanguageTag("ar")    // Arabic / Urdu
            text.any { it in '\u4E00'..'\u9FFF' } -> Locale.CHINESE                 // Chinese
            text.any { it in '\u3040'..'\u30FF' } -> Locale.JAPANESE                // Japanese
            text.any { it in '\uAC00'..'\uD7AF' } -> Locale.KOREAN                  // Korean
            text.any { it in '\u0400'..'\u04FF' } -> Locale.forLanguageTag("ru-RU") // Russian / Cyrillic
            text.any { it in '\u0A80'..'\u0AFF' } -> Locale.forLanguageTag("gu-IN") // Gujarati
            text.any { it in '\u0B80'..'\u0BFF' } -> Locale.forLanguageTag("ta-IN") // Tamil
            text.any { it in '\u0C00'..'\u0C7F' } -> Locale.forLanguageTag("te-IN") // Telugu
            text.any { it in '\u0D00'..'\u0D7F' } -> Locale.forLanguageTag("ml-IN") // Malayalam
            text.any { it in '\u0A00'..'\u0A7F' } -> Locale.forLanguageTag("pa-IN") // Punjabi
            else -> {
                val lower = text.lowercase()
                when {
                    lower.contains(" por favor") || lower.contains("hola") || lower.contains("gracias") -> Locale.forLanguageTag("es-ES")
                    lower.contains(" s'il vous") || lower.contains("bonjour") || lower.contains("merci") -> Locale.FRENCH
                    lower.contains(" danke") || lower.contains("guten tag") || lower.contains("bitte") -> Locale.GERMAN
                    lower.contains(" obrigado") || lower.contains("olá") || lower.contains("bom dia") -> Locale.forLanguageTag("pt-BR")
                    lower.contains(" grazie") || lower.contains("buongiorno") || lower.contains("ciao") -> Locale.ITALIAN
                    else -> Locale.UK // Refined butler British English
                }
            }
        }
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("```[\\s\\S]*?```"), " Code block omitted for voice reply. ")
            .replace(Regex("`[^`]*`"), "")
            .replace(Regex("[#*_~`>]"), "")
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) {
            onFinished?.invoke()
            return
        }
        stopSpeaking()
        onSpeakingFinishedCallback = onFinished

        val cleanText = cleanTextForSpeech(text)
        if (cleanText.isBlank()) {
            onFinished?.invoke()
            return
        }

        // Dynamically select target language locale
        val targetLocale = detectLanguageLocale(cleanText)
        try {
            val supported = tts?.isLanguageAvailable(targetLocale)
            if (supported != null && supported >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = targetLocale
                val availableVoices = tts?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val softVoice = availableVoices.firstOrNull { voice ->
                        voice.locale.language == targetLocale.language &&
                        (voice.name.contains("male", ignoreCase = true) || !voice.isNetworkConnectionRequired)
                    }
                    if (softVoice != null) {
                        tts?.voice = softVoice
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Locale setup error: ${e.message}")
        }

        // Ensure soft polite pitch, relaxed pacing, and calm volume
        tts?.setPitch(0.85f)
        tts?.setSpeechRate(0.90f)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.82f) // Very soft, polite volume
        }
        val utteranceId = "jarvis_speech_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
        onSpeakingFinishedCallback = null
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition is not available on this device.")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = (rmsdB / 10f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Recognition code: $error"
                        }
                        Log.w(TAG, "SpeechRecognizer error: $message")
                        onError?.invoke(message)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull().orEmpty()
                        if (spokenText.isNotBlank()) {
                            onResult(spokenText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(
                    "en", "hi", "es", "fr", "de", "ar", "zh", "ja", "ru", "pt", "bn", "ur", "it"
                ))
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _isListening.value = false
            onError?.invoke(e.localizedMessage ?: "Failed to start speech recognition.")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping SpeechRecognizer", e)
        }
        speechRecognizer = null
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun release() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
        tts = null
    }
}
