package com.parth.aquasense.presentation.screens.voiceagent

import com.parth.aquasense.domain.model.VoiceMessage

/**
 * UI State for Voice Agent Screen
 */
sealed class VoiceAgentUiState {
    /**
     * Initial state - not connected
     */
    data object Disconnected : VoiceAgentUiState()

    /**
     * Connecting to WebSocket
     */
    data object Connecting : VoiceAgentUiState()

    /**
     * Connected and ready
     */
    data class Connected(
        val sessionId: String,
        val messages: List<VoiceMessage> = emptyList(),
        val currentTranscript: String = "",
        val isListening: Boolean = false,
        val isSpeaking: Boolean = false,
        val isThinking: Boolean = false
    ) : VoiceAgentUiState()

    /**
     * Error state
     */
    data class Error(val message: String) : VoiceAgentUiState()
}
