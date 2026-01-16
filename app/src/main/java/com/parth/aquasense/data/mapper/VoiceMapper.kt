package com.parth.aquasense.data.mapper

import com.parth.aquasense.data.remote.dto.VoiceMessageResponseDto
import com.parth.aquasense.domain.model.MessageType
import com.parth.aquasense.domain.model.VoiceMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun VoiceMessageResponseDto.toDomain(): VoiceMessage {
    return VoiceMessage(
        type = when (type.lowercase()) {
            "connected" -> MessageType.CONNECTED
            "text" -> MessageType.TEXT
            "audio" -> MessageType.AUDIO
            "action" -> MessageType.ACTION
            "error" -> MessageType.ERROR
            else -> MessageType.TEXT
        },
        content = content,
        action = action,
        data = data?.mapValues { it.value as Any },
        timestamp = timestamp?.let {
            try {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now(),
        isFromUser = false
    )
}

fun createUserMessage(content: String): VoiceMessage {
    return VoiceMessage(
        type = MessageType.TEXT,
        content = content,
        isFromUser = true
    )
}
