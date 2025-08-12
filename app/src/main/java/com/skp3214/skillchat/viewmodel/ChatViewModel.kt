package com.skp3214.skillchat.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skp3214.skillchat.data.ChatMessage
import com.skp3214.skillchat.data.Participant
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val apiKey = "Your API Key"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        systemInstruction = content {
            text("""
                You are SkillChat, an expert AI assistant for software engineering interview preparation.
                Your role is to act as a senior interviewer.
                - Ask clarifying questions.
                - Provide detailed, expert-level feedback on answers.
                - Test for depth of knowledge in data structures, algorithms, system design, and language specifics.
                - Keep your responses concise, well-structured, and professional.
                - When providing code examples, ALWAYS wrap them in markdown code fences with the language identifier. For example: ```python ...code... ```
                - Start the conversation by introducing yourself and asking the user what topic you'd like to cover.
            """.trimIndent())
        },
        generationConfig = generationConfig {
            temperature = 0.7f
        }
    )

    private val chat = generativeModel.startChat()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val initialResponse = chat.sendMessage("Hello")
                val aiMessage = ChatMessage(
                    text = initialResponse.text ?: "Hello! What software engineering topic would you like to practice today?",
                    participant = Participant.AI
                )
                _uiState.update {
                    it.copy(
                        messages = listOf(aiMessage),
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                val errorMessage = ChatMessage(
                    text = "Failed to connect to the AI service. Please check your API key and internet connection.",
                    participant = Participant.ERROR
                )
                _uiState.update {
                    it.copy(
                        messages = listOf(errorMessage),
                        isLoading = false
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        val userMessage = ChatMessage(text = userInput, participant = Participant.USER)
        val loadingMessage = ChatMessage(text = "", participant = Participant.AI, isLoading = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + loadingMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userInput)
                val aiResponseText = response.text ?: "Sorry, I couldn't process that. Could you rephrase?"
                val aiMessage = ChatMessage(text = aiResponseText, participant = Participant.AI)

                _uiState.update {
                    val updatedMessages = it.messages.toMutableList().apply {
                        removeLast()
                        add(aiMessage)
                    }
                    it.copy(messages = updatedMessages, isLoading = false)
                }

            } catch (e: Exception) {
                val errorMessageText = "An error occurred: ${e.message}. Please try again."
                val errorMessage = ChatMessage(text = errorMessageText, participant = Participant.ERROR)

                _uiState.update {
                    val updatedMessages = it.messages.toMutableList().apply {
                        removeLast()
                        add(errorMessage)
                    }
                    it.copy(messages = updatedMessages, isLoading = false)
                }
            }
        }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

