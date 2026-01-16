package com.parth.aquasense.domain.model

import java.time.LocalDateTime

/**
 * Domain model for voice agent messages
 */
data class VoiceMessage(
    val type: MessageType,
    val content: String,
    val action: String? = null,
    val data: Map<String, Any>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isFromUser: Boolean
)

enum class MessageType {
    CONNECTED,
    TEXT,
    AUDIO,
    ACTION,
    ERROR
}
