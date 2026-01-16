package com.parth.aquasense.domain.model

/**
 * Represents the state of a voice session
 */
data class VoiceSessionState(
    val sessionId: String,
    val isConnected: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val currentTranscript: String = "",
    val messages: List<VoiceMessage> = emptyList()
)
