package com.parth.aquasense.presentation.screens.tankdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.parth.aquasense.presentation.components.WaterQualityGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.TankStatus
import com.parth.aquasense.domain.model.WaterQuality
import com.parth.aquasense.presentation.components.TankAnalysisBottomSheet
import com.parth.aquasense.presentation.components.WaterQualityCard
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Tank Detail Screen
 * Shows individual tank details, water quality readings, and edit/delete actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddReading: (String) -> Unit,
    viewModel: TankDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle navigation after successful deletion
    LaunchedEffect(uiState) {
        if (uiState is TankDetailUiState.Deleted) {
            onNavigateBack()
        }
    }

    // Show snackbar for analysis errors
    LaunchedEffect(uiState) {
        if (uiState is TankDetailUiState.Success) {
            val state = uiState as TankDetailUiState.Success
            state.analysisError?.let { error ->
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TankDetailTopBar(
                title = when (val state = uiState) {
                    is TankDetailUiState.Success -> state.tank.name
                    else -> "Tank Details"
                },
                onNavigateBack = onNavigateBack,
                onEdit = {
                    if (uiState is TankDetailUiState.Success) {
                        onNavigateToEdit((uiState as TankDetailUiState.Success).tank.id)
                    }
                },
                onDelete = {
                    // Will be handled by confirmation dialog
                },
                showActions = uiState is TankDetailUiState.Success
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TankDetailUiState.Loading -> {
                LoadingContent(paddingValues)
            }

            is TankDetailUiState.Success -> {
                SuccessContent(
                    tank = state.tank,
                    waterQuality = state.waterQuality,
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    onDelete = viewModel::deleteTank,
                    analysis = state.analysis,
                    isLoadingAnalysis = state.isLoadingAnalysis,
                    onGetAnalysis = viewModel::loadTankAnalysis,
                    onDismissAnalysis = viewModel::dismissAnalysis,
                    paddingValues = paddingValues
                )
            }

            is TankDetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    paddingValues = paddingValues
                )
            }

            is TankDetailUiState.Deleted -> {
                // Navigation handled by LaunchedEffect
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TankDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showActions: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (showActions) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Tank"
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Tank",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0)
    )

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tank?") },
            text = { Text("Are you sure you want to delete this tank? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    tank: Tank,
    waterQuality: WaterQuality?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    analysis: com.parth.aquasense.domain.model.TankAnalysis?,
    isLoadingAnalysis: Boolean,
    onGetAnalysis: () -> Unit,
    onDismissAnalysis: () -> Unit,
    paddingValues: PaddingValues
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // New Header
            TankDetailHeader(tank = tank)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp), // Added bottom padding
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Water Quality Grid
                if (waterQuality != null) {
                    WaterQualityGrid(waterQuality = waterQuality)
                } else {
                    EmptyWaterQualityState(
                        onAddReading = { /* handled by FAB usually */ }
                    )
                }

                // AI Recommendations Button
                Button(
                    onClick = onGetAnalysis,
                    enabled = !isLoadingAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(height = 56.dp, width = 0.dp), // Height 56dp
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoadingAnalysis) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Getting AI Recommendations...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Get AI Recommendations")
                    }
                }

                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }

    // Show analysis bottom sheet when available
    if (analysis != null) {
        TankAnalysisBottomSheet(
            analysis = analysis,
            onDismiss = onDismissAnalysis,
            onShare = {
                // TODO: Implement share functionality
                onDismissAnalysis()
            }
        )
    }
}

@Composable
private fun TankDetailHeader(tank: Tank) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(height = 280.dp, width = 0.dp) // Large header space
    ) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8E8E93),
                            Color(0xFF636366)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(32.dp)
        ) {
            // Icon Removed as per request

            Spacer(modifier = Modifier.size(16.dp))

            // Tank Name
            Text(
                text = tank.name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.size(4.dp))

            // Location • Species
            Text(
                text = "${tank.location ?: "Unknown Location"} • ${tank.speciesDisplay}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EmptyWaterQualityState(
    onAddReading: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Water,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No Water Quality Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add water quality readings to monitor tank health",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TankDetailScreenPreview() {
    AquaSenseTheme {
        SuccessContent(
            tank = Tank(
                id = "1",
                userId = "user1",
                name = "Main Pond",
                capacity = 5000.0,
                currentStock = 150,
                species = listOf("Tilapia", "Catfish"),
                location = "Greenhouse A, Section 2",
                status = TankStatus.ACTIVE,
                createdAt = "2025-12-01T10:00:00Z",
                updatedAt = "2025-12-20T15:30:00Z"
            ),
            waterQuality = WaterQuality(
                id = "1",
                tankId = "1",
                ph = 7.2,
                temperature = 26.5,
                dissolvedOxygen = 6.5,
                ammonia = 0.1,
                nitrite = 0.05,
                nitrate = 10.0,
                salinity = 35.0,
                turbidity = 2.5,
                createdAt = "2025-12-24T10:30:00Z"
            ),
            isRefreshing = false,
            onRefresh = {},
            onDelete = {},
            analysis = null,
            isLoadingAnalysis = false,
            onGetAnalysis = {},
            onDismissAnalysis = {},
            paddingValues = PaddingValues(0.dp)
        )
    }
}
