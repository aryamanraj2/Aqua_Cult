package com.parth.aquasense.data.repository

import com.parth.aquasense.data.mapper.toDomain
import com.parth.aquasense.data.remote.websocket.VoiceAgentWebSocket
import com.parth.aquasense.domain.model.VoiceMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepository @Inject constructor(
    private val webSocket: VoiceAgentWebSocket
) {
    fun connectToVoiceAgent(sessionId: String) {
        webSocket.connect(sessionId)
    }

    fun sendTextMessage(content: String, metadata: Map<String, String>? = null) {
        webSocket.sendMessage(content, metadata)
    }

    fun getMessages(): Flow<VoiceMessage> {
        return webSocket.messages.map { it.toDomain() }
    }

    fun getConnectionState(): Flow<VoiceAgentWebSocket.ConnectionState> {
        return webSocket.connectionState
    }

    fun disconnect() {
        webSocket.disconnect()
    }
}
