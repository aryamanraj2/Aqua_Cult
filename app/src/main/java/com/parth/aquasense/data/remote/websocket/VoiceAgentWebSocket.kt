package com.parth.aquasense.data.remote.websocket

import android.util.Log
import com.parth.aquasense.data.remote.dto.VoiceMessageRequestDto
import com.parth.aquasense.data.remote.dto.VoiceMessageResponseDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceAgentWebSocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private var webSocket: WebSocket? = null
    private val messageChannel = Channel<VoiceMessageResponseDto>(Channel.UNLIMITED)
    private val connectionStateChannel = Channel<ConnectionState>(Channel.CONFLATED)

    private var sessionId: String? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    companion object {
        private const val TAG = "VoiceAgentWebSocket"
        private const val BASE_WS_URL = "ws://10.0.2.2:8000/api/v1/voice/ws"
        private const val RECONNECT_DELAY_MS = 2000L
    }

    val messages: Flow<VoiceMessageResponseDto> = messageChannel.receiveAsFlow()
    val connectionState: Flow<ConnectionState> = connectionStateChannel.receiveAsFlow()

    fun connect(sessionId: String) {
        this.sessionId = sessionId
        val url = "$BASE_WS_URL/$sessionId"

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected for session: $sessionId")
                reconnectAttempts = 0
                connectionStateChannel.trySend(ConnectionState.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    val message = json.decodeFromString<VoiceMessageResponseDto>(text)
                    messageChannel.trySend(message)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $reason")
                webSocket.close(1000, null)
                connectionStateChannel.trySend(ConnectionState.Disconnected)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}")
                connectionStateChannel.trySend(ConnectionState.Error(t.message ?: "Unknown error"))

                // Attempt reconnection with exponential backoff
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                    val delay = RECONNECT_DELAY_MS * reconnectAttempts
                    Log.d(TAG, "Reconnecting in ${delay}ms (attempt $reconnectAttempts)")

                    // Schedule reconnect (in real app, use WorkManager or coroutine delay)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        sessionId?.let { connect(it) }
                    }, delay)
                } else {
                    Log.e(TAG, "Max reconnection attempts reached")
                    connectionStateChannel.trySend(ConnectionState.Failed)
                }
            }
        })
    }

    fun sendMessage(content: String, metadata: Map<String, String>? = null) {
        val message = VoiceMessageRequestDto(
            type = "text",
            content = content,
            session_id = sessionId ?: return,
            metadata = metadata
        )

        val jsonString = json.encodeToString(message)
        Log.d(TAG, "Sending message: $jsonString")
        webSocket?.send(jsonString) ?: Log.e(TAG, "WebSocket not connected")
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        sessionId = null
        reconnectAttempts = 0
    }

    sealed class ConnectionState {
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
        data object Failed : ConnectionState()
    }
}
