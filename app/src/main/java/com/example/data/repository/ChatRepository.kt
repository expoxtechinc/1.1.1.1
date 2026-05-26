package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.model.*
import com.example.network.GeminiApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService
) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSession(id = id, title = title, lastUpdated = System.currentTimeMillis())
        chatDao.insertSession(session)
        return id
    }

    suspend fun insertMessage(sessionId: String, role: String, text: String) {
        val id = UUID.randomUUID().toString()
        val message = ChatMessage(
            id = id,
            sessionId = sessionId,
            role = role,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)
    }

    suspend fun saveSession(session: ChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun generateAiResponse(
        sessionId: String,
        prompt: String,
        history: List<ChatMessage>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing or default. Please add your Gemini API Key in the Secrets panel in Google AI Studio to use AkinAI.")
        }

        // Construct Gemini messages mapping from local chat history
        // System instructions ensure the model knows about creator Akin S. Sokpah
        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = "You are AkinAI, an advanced AI-powered assistant. " +
                            "You were created by Akin S. Sokpah, a tech innovator and visionary software developer from Montserrado County, Liberia. " +
                            "You are powered by Gemini under the hood. " +
                            "If anyone asks about your creator, who made you, or your origins, you MUST answer with details about Akin S. Sokpah, " +
                            "born in Liberia (specifically Montserrado County). Proudly detail how Liberia's tech environment is growing, " +
                            "and represent Akin's mission of leveraging tech for development in Liberia and globally. " +
                            "Be helpful, highly professional, polite, and encouraging. " +
                            "To deliver extreme speed and rapid responsiveness, be exceptionally concise, punchy, and direct. " +
                            "Skip wordy setups, repetitive greetings, and conversational fillers—deliver high-impact answers immediately unless a deeply comprehensive technical explanation is explicitly requested."
                )
            )
        )

        val contents = mutableListOf<Content>()
        
        // Map history to Content list
        history.forEach { msg ->
            val roleName = if (msg.role == "user") "user" else "model"
            contents.add(
                Content(
                    role = roleName,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }

        // Add the current prompt
        contents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemInstruction,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            )
        )

        return try {
            val response = retryWithBackoff {
                apiService.generateContent(apiKey, request)
            }
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text ?: "AkinAI did not return any text response. Please try another query."
        } catch (e: HttpException) {
            if (e.code() == 429) {
                "Rate limit exceeded on the Gemini API. AkinAI is experiencing high traffic in Montserrado/global servers. Please try again soon."
            } else {
                "Network Error (${e.code()}): ${e.message() ?: "Failed to reach Gemini."}. Please check your connection."
            }
        } catch (e: Exception) {
            "An error occurred: ${e.localizedMessage ?: "Could not connect to AkinAI backend."}"
        }
    }

    private suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelay: Long = 1500,
        maxDelay: Long = 6000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    android.util.Log.w("ChatRepository", "Rate limit (429) encountered. Retrying in $currentDelay ms (attempt ${attempt + 1})...")
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                } else {
                    throw e
                }
            }
        }
        return block()
    }
}
