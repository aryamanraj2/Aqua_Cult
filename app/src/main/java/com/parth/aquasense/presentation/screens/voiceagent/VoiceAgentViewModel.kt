package com.parth.aquasense.presentation.screens.voiceagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.remote.websocket.VoiceAgentWebSocket
import com.parth.aquasense.data.repository.VoiceRepository
import com.parth.aquasense.domain.model.MessageType
import com.parth.aquasense.domain.model.VoiceMessage
import com.parth.aquasense.util.AudioPlaybackManager
import com.parth.aquasense.util.SpeechRecognitionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VoiceAgentViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val tankRepository: com.parth.aquasense.data.repository.TankRepository,
    private val speechManager: SpeechRecognitionManager,
    private val audioManager: AudioPlaybackManager,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceAgentUiState>(VoiceAgentUiState.Disconnected)
    val uiState: StateFlow<VoiceAgentUiState> = _uiState.asStateFlow()

    private val sessionId = UUID.randomUUID().toString()
    private val messages = mutableListOf<VoiceMessage>()

    // Get tankId from navigation arguments (optional - for focused tank context)
    private val tankId: String? = savedStateHandle.get<String>("tankId")

    // Store all tanks for context
    private var allTanks: List<com.parth.aquasense.domain.model.Tank> = emptyList()

    init {
        initializeServices()
        fetchAllTanks()
        connectToVoiceAgent()
        observeWebSocketMessages()
        observeSpeechTranscripts()
        observeAudioPlayback()
    }

    private fun fetchAllTanks() {
        viewModelScope.launch {
            when (val result = tankRepository.getTanks()) {
                is com.parth.aquasense.domain.util.Result.Success -> {
                    allTanks = result.data
                }
                is com.parth.aquasense.domain.util.Result.Error -> {
                    // Log error but don't fail - voice agent can still work for general questions
                    android.util.Log.e("VoiceAgentVM", "Failed to fetch tanks: ${result.message}")
                }
            }
        }
    }

    private fun initializeServices() {
        speechManager.initialize()
        audioManager.initialize()
    }

    private fun connectToVoiceAgent() {
        _uiState.value = VoiceAgentUiState.Connecting
        voiceRepository.connectToVoiceAgent(sessionId)

        viewModelScope.launch {
            voiceRepository.getConnectionState().collect { state ->
                when (state) {
                    is VoiceAgentWebSocket.ConnectionState.Connected -> {
                        _uiState.value = VoiceAgentUiState.Connected(sessionId = sessionId)
                    }
                    is VoiceAgentWebSocket.ConnectionState.Error -> {
                        _uiState.value = VoiceAgentUiState.Error(state.message)
                    }
                    is VoiceAgentWebSocket.ConnectionState.Failed -> {
                        _uiState.value = VoiceAgentUiState.Error("Connection failed after multiple attempts")
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            voiceRepository.getMessages().collect { message ->
                messages.add(message)
                updateConnectedState(isThinking = false)

                // TODO: Play TTS audio if available
                // For now, just update UI
            }
        }
    }

    private fun observeSpeechTranscripts() {
        viewModelScope.launch {
            speechManager.transcripts.collect { result ->
                when (result) {
                    is SpeechRecognitionManager.TranscriptResult.Partial -> {
                        updateConnectedState(currentTranscript = result.text)
                    }
                    is SpeechRecognitionManager.TranscriptResult.Final -> {
                        // Add user message
                        val userMessage = VoiceMessage(
                            type = MessageType.TEXT,
                            content = result.text,
                            isFromUser = true
                        )
                        messages.add(userMessage)

                        // Send to backend with comprehensive tank context
                        val metadata = buildMetadata()
                        voiceRepository.sendTextMessage(result.text, metadata)

                        // Update UI
                        updateConnectedState(
                            currentTranscript = "",
                            isListening = false,
                            isThinking = true
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            speechManager.state.collect { state ->
                when (state) {
                    is SpeechRecognitionManager.RecognitionState.Error -> {
                        updateConnectedState(
                            isListening = false,
                            currentTranscript = ""
                        )
                        // Could show error in UI
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    private fun observeAudioPlayback() {
        viewModelScope.launch {
            audioManager.playbackState.collect { state ->
                when (state) {
                    is AudioPlaybackManager.PlaybackState.Playing -> {
                        updateConnectedState(isSpeaking = true)
                    }
                    is AudioPlaybackManager.PlaybackState.Completed,
                    is AudioPlaybackManager.PlaybackState.Idle -> {
                        updateConnectedState(isSpeaking = false)
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    fun startListening() {
        speechManager.startListening()
        updateConnectedState(isListening = true)
    }

    fun stopListening() {
        speechManager.stopListening()
        updateConnectedState(isListening = false)
    }

    fun sendTextMessage(text: String) {
        val userMessage = VoiceMessage(
            type = MessageType.TEXT,
            content = text,
            isFromUser = true
        )
        messages.add(userMessage)

        // Send to backend with comprehensive tank context
        val metadata = buildMetadata()
        voiceRepository.sendTextMessage(text, metadata)

        updateConnectedState(isThinking = true)
    }

    /**
     * Build metadata with tank context for the voice agent
     * Includes:
     * - Primary tank_id if navigated from tank detail
     * - All tanks data as JSON (full tank details for Gemini context)
     */
    private fun buildMetadata(): Map<String, String>? {
        if (allTanks.isEmpty() && tankId == null) {
            return null
        }

        val metadata = mutableMapOf<String, String>()

        // Add primary tank if specified (for focused context)
        tankId?.let { metadata["primary_tank_id"] = it }

        // Send full tank data as JSON for Gemini context
        // This allows the AI to answer questions about all tanks
        if (allTanks.isNotEmpty()) {
            try {
                // Build JSON string manually to avoid serialization type issues
                val tanksJsonArray = allTanks.joinToString(separator = ",", prefix = "[", postfix = "]") { tank ->
                    """
                    {
                        "id": "${tank.id}",
                        "name": "${tank.name}",
                        "species": "${tank.species.joinToString(", ")}",
                        "capacity": ${tank.capacity},
                        "current_stock": ${tank.currentStock},
                        "location": "${tank.location ?: "Not specified"}",
                        "status": "${tank.status.displayName}"
                    }
                    """.trimIndent().replace("\n", "").replace("  ", "")
                }
                metadata["all_tanks_data"] = tanksJsonArray
            } catch (e: Exception) {
                android.util.Log.e("VoiceAgentVM", "Failed to serialize tanks: ${e.message}")
                // Fallback: send just IDs
                metadata["all_tank_ids"] = allTanks.joinToString("|") { it.id }
            }
        }

        return metadata.ifEmpty { null }
    }

    fun retry() {
        connectToVoiceAgent()
    }

    private fun updateConnectedState(
        currentTranscript: String? = null,
        isListening: Boolean? = null,
        isSpeaking: Boolean? = null,
        isThinking: Boolean? = null
    ) {
        val currentState = _uiState.value
        if (currentState is VoiceAgentUiState.Connected) {
            _uiState.value = currentState.copy(
                messages = messages.toList(),
                currentTranscript = currentTranscript ?: currentState.currentTranscript,
                isListening = isListening ?: currentState.isListening,
                isSpeaking = isSpeaking ?: currentState.isSpeaking,
                isThinking = isThinking ?: currentState.isThinking
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRepository.disconnect()
        speechManager.destroy()
        audioManager.release()
    }
}
