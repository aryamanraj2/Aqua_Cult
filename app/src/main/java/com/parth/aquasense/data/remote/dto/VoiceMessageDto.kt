package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VoiceMessageRequestDto(
    val type: String,
    val content: String,
    val session_id: String,
    val metadata: Map<String, String>? = null
)

@Serializable
data class VoiceMessageResponseDto(
    val type: String,
    val content: String,
    val action: String? = null,
    val data: Map<String, String>? = null,
    val timestamp: String? = null,
    val session_id: String? = null,
    val error: String? = null
)
