package com.parth.aquasense.presentation.screens.voiceagent

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parth.aquasense.presentation.components.VoiceMicButton
import com.parth.aquasense.presentation.components.VoiceTranscriptBubble
import com.parth.aquasense.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAgentScreen(
    viewModel: VoiceAgentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Permission handling
    var hasRecordAudioPermission by remember {
        mutableStateOf(PermissionHelper.hasRecordAudioPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasRecordAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Assistant") },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is VoiceAgentUiState.Disconnected -> {
                DisconnectedState(modifier = Modifier.padding(paddingValues))
            }

            is VoiceAgentUiState.Connecting -> {
                ConnectingState(modifier = Modifier.padding(paddingValues))
            }

            is VoiceAgentUiState.Connected -> {
                ConnectedState(
                    state = state,
                    hasPermission = hasRecordAudioPermission,
                    onMicToggle = {
                        if (state.isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is VoiceAgentUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DisconnectedState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ConnectingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connecting to voice assistant...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ConnectedState(
    state: VoiceAgentUiState.Connected,
    hasPermission: Boolean,
    onMicToggle: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.messages, key = { it.timestamp.toString() }) { message ->
                VoiceTranscriptBubble(
                    text = message.content,
                    isFromUser = message.isFromUser
                )
            }

            // Current transcript (partial)
            if (state.currentTranscript.isNotEmpty()) {
                item {
                    VoiceTranscriptBubble(
                        text = state.currentTranscript,
                        isFromUser = true
                    )
                }
            }

            // Thinking indicator
            if (state.isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status text
            if (state.isListening) {
                Text(
                    text = "Listening...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (state.isSpeaking) {
                Text(
                    text = "Speaking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "Tap the mic to speak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mic button or permission request
            if (hasPermission) {
                VoiceMicButton(
                    isListening = state.isListening,
                    onToggle = onMicToggle
                )
            } else {
                Button(onClick = onRequestPermission) {
                    Text("Grant Microphone Permission")
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Connection Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Connection")
            }
        }
    }
}
