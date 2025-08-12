package com.skp3214.skillchat.data

data class ChatMessage(
    val text: String,
    val participant: Participant,
    val isLoading: Boolean = false
)

enum class Participant {
    USER, AI, ERROR
}

