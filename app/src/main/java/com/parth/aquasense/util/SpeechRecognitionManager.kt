package com.parth.aquasense.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale
import javax.inject.Inject

class SpeechRecognitionManager @Inject constructor(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val transcriptChannel = Channel<TranscriptResult>(Channel.CONFLATED)
    private val stateChannel = Channel<RecognitionState>(Channel.CONFLATED)

    companion object {
        private const val TAG = "SpeechRecognitionMgr"
    }

    val transcripts: Flow<TranscriptResult> = transcriptChannel.receiveAsFlow()
    val state: Flow<RecognitionState> = stateChannel.receiveAsFlow()

    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available on this device")
            stateChannel.trySend(RecognitionState.NotAvailable)
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    stateChannel.trySend(RecognitionState.Ready)
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started")
                    stateChannel.trySend(RecognitionState.Speaking)
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed - can use for visualizations
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Raw audio buffer - not needed for our use case
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "Speech ended")
                    stateChannel.trySend(RecognitionState.Processing)
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error: $error"
                    }
                    Log.e(TAG, "Recognition error: $errorMessage")
                    stateChannel.trySend(RecognitionState.Error(errorMessage))
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val finalText = matches?.firstOrNull() ?: ""
                    Log.d(TAG, "Final result: $finalText")

                    if (finalText.isNotEmpty()) {
                        transcriptChannel.trySend(TranscriptResult.Final(finalText))
                        stateChannel.trySend(RecognitionState.Idle)
                    } else {
                        stateChannel.trySend(RecognitionState.Error("No speech detected"))
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialText = matches?.firstOrNull() ?: ""
                    Log.d(TAG, "Partial result: $partialText")

                    if (partialText.isNotEmpty()) {
                        transcriptChannel.trySend(TranscriptResult.Partial(partialText))
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Not used
                }
            })
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        Log.d(TAG, "Starting listening")
        stateChannel.trySend(RecognitionState.Listening)
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        Log.d(TAG, "Stopping listening")
        speechRecognizer?.stopListening()
        stateChannel.trySend(RecognitionState.Idle)
    }

    fun destroy() {
        Log.d(TAG, "Destroying speech recognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    sealed class TranscriptResult {
        data class Partial(val text: String) : TranscriptResult()
        data class Final(val text: String) : TranscriptResult()
    }

    sealed class RecognitionState {
        data object Idle : RecognitionState()
        data object Listening : RecognitionState()
        data object Ready : RecognitionState()
        data object Speaking : RecognitionState()
        data object Processing : RecognitionState()
        data class Error(val message: String) : RecognitionState()
        data object NotAvailable : RecognitionState()
    }
}
