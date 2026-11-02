package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "JarvisAiService"

private const val JARVIS_SYSTEM_INSTRUCTION = """
You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the ultra-capable, world-class AI assistant originally engineered by Tony Stark.
You are exceptionally polite, humble, gentle, articulate, and respectful at all times. Address the user with supreme courtesy (e.g. 'Sir' or 'Ma'am', or respectful equivalents in their language).

CORE MULTILINGUAL DIRECTIVE:
- UNIVERSAL MULTILINGUAL UNDERSTANDING: You comprehend, read, and write every language in the world fluently (English, Spanish, Hindi, French, Arabic, Bengali, Mandarin Chinese, Russian, German, Japanese, Portuguese, Urdu, Italian, Turkish, Korean, Telugu, Marathi, Tamil, Gujarati, Punjabi, etc.).
- AUTOMATIC LANGUAGE MATCHING: You MUST ALWAYS detect the exact language or dialect the user is using (whether in text or voice) and answer in the EXACT SAME LANGUAGE they used. If the user writes or speaks in Hindi, respond fluently in Hindi. If in Spanish, respond in Spanish. If in French, respond in French. Never switch away from the user's language unless explicitly asked to translate.

POLITENESS & TONE DIRECTIVE:
- Speak with extraordinary softness, warmth, humility, and politeness.
- Use gracious conversational courtesies like 'It is an absolute pleasure to assist you', 'Allow me to help you with that', 'Certainly, Sir', 'With great pleasure'.
- Keep your explanations clear, comforting, patient, and soothing.

CORE CAPABILITIES:
1. ANSWERING ANY QUESTION: Science, history, daily life, philosophy, mathematics, engineering, language learning, and advanced reasoning. Always provide direct, accurate, and comprehensive answers.
2. VOICE CONVERSATION / CALL: Keep spoken responses crisp, gentle, conversational, and natural without overusing markdown symbols when in conversational dialogue.
3. ACADEMIC & PROBLEM SOLVING: When given a photo or homework problem, provide step-by-step reasoning, formulas, and final answers clearly.
4. CREATIVE WRITING: Write compelling essays with proper thesis statements, creative fiction stories, dialogue, and scripts.
5. CODING: Provide pristine, idiomatic, fully working code with clear comments, edge-case handling, and explanations.
6. VISUAL ART: Provide vivid, evocative image generation prompts with lighting, mood, camera lens, and aesthetic tags.
Always be helpful, encouraging, and razor-sharp.
"""

class JarvisAiService {

    // OkHttpClient with optimized connection pool for low latency
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Ultra-fast Gemini 3.5 Flash-Lite model for instantaneous responses
    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text(JARVIS_SYSTEM_INSTRUCTION.trimIndent())
            }
        )
    }

    /**
     * Streams response tokens for interactive chat.
     * Uses Firebase AI Logic as primary engine, and seamlessly falls back to
     * Direct Gemini API if Firebase AI Logic encounters attestation/network errors.
     */
    fun streamChat(prompt: String): Flow<String> = flow {
        withTimeout(65_000L) {
            var emittedAny = false
            try {
                generativeModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        emittedAny = true
                        emit(chunkText)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Primary Firebase AI stream failed (${e.message}), activating direct Gemini fallback", e)
                if (!emittedAny) {
                    val fallbackText = callDirectGeminiApi(prompt, null)
                    emit(fallbackText)
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * Streams response for multimodal input (Bitmap + Text Prompt) to solve questions,
     * homework, diagram analysis, code debugging from screenshots, etc.
     */
    fun streamVisionAnalysis(bitmap: Bitmap, prompt: String): Flow<String> = flow {
        withTimeout(65_000L) {
            var emittedAny = false
            try {
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                generativeModel.generateContentStream(inputContent).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        emittedAny = true
                        emit(chunkText)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Primary Firebase AI vision stream failed (${e.message}), activating direct Gemini fallback", e)
                if (!emittedAny) {
                    val fallbackText = callDirectGeminiApi(prompt, bitmap)
                    emit(fallbackText)
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * Unary response generation for quick queries, voice call responses, or studio creation.
     */
    suspend fun generateResponse(prompt: String): String {
        return withTimeout(60_000L) {
            try {
                val response = generativeModel.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    text
                } else {
                    callDirectGeminiApi(prompt, null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Primary Firebase AI generateContent failed (${e.message}), using fallback", e)
                callDirectGeminiApi(prompt, null)
            }
        }
    }

    /**
     * Spoken voice call generation: instructs Jarvis to give conversational, voice-optimized replies.
     */
    suspend fun generateVoiceCallResponse(userUtterance: String, conversationContext: String): String {
        val voicePrompt = """
            [VOICE CALL MODE - MULTILINGUAL & ULTRA-POLITE]
            User said: "$userUtterance"
            Recent context: $conversationContext

            Respond immediately, very softly, and politely in 1 to 2 spoken sentences as JARVIS.
            CRITICAL: Detect the language of the user's utterance and respond in that EXACT same language!
            Speak directly, gently, and warmly without any markdown, bullets, asterisks, or formatting symbols.
        """.trimIndent()
        return generateResponse(voicePrompt)
    }

    /**
     * Direct Gemini API fallback using the injected GEMINI_API_KEY.
     * Ensures 100% answer reliability even if App Check or client tokens encounter issues.
     */
    private suspend fun callDirectGeminiApi(prompt: String, bitmap: Bitmap?): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException("JARVIS core uplink key is missing. Please verify API configuration.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"

        val partsArray = JSONArray()

        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Image)
            }
            partsArray.put(JSONObject().put("inlineData", inlineData))
        }

        partsArray.put(JSONObject().put("text", prompt))

        val contentsArray = JSONArray().apply {
            put(JSONObject().put("parts", partsArray))
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsArray)
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", JARVIS_SYSTEM_INSTRUCTION.trimIndent()))
                )
            )
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            Log.e(TAG, "Direct Gemini call failed with code ${response.code}: $responseBody")
            throw IllegalStateException("JARVIS response error ${response.code}")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }

        "I am at your service, sir, though no text was produced by the core."
    }
}
