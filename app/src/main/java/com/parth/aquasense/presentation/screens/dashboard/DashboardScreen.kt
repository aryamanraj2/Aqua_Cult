package com.parth.aquasense.presentation.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.Alert
import com.parth.aquasense.domain.model.AlertType
import com.parth.aquasense.domain.model.Dashboard
import com.parth.aquasense.presentation.components.AlertCard
import com.parth.aquasense.presentation.components.WeatherCard
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Dashboard Screen
 * Shows overview statistics, recent alerts, and quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTanks: () -> Unit,
    onNavigateToAddTank: () -> Unit,
    onNavigateToTankDetail: (String) -> Unit,
    onNavigateToVoiceAgent: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWeatherSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aqua Sense", fontWeight = FontWeight.Bold, fontSize = 35.sp) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToVoiceAgent,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Assistant"
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                LoadingContent(paddingValues)
            }

            is DashboardUiState.Success -> {
                SuccessContent(
                    dashboard = state.dashboard,
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    onNavigateToTanks = onNavigateToTanks,
                    onNavigateToAddTank = onNavigateToAddTank,
                    onNavigateToTankDetail = onNavigateToTankDetail,
                    onWeatherClick = { showWeatherSheet = true },
                    paddingValues = paddingValues
                )
            }

            is DashboardUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    paddingValues = paddingValues
                )
            }
        }
    }

    
    if (showWeatherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWeatherSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent
        ) {
            WeatherDetailBottomSheet(
                onDismiss = { showWeatherSheet = false }
            )
        }
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
    dashboard: Dashboard,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToTanks: () -> Unit,
    onNavigateToAddTank: () -> Unit,
    onNavigateToTankDetail: (String) -> Unit,
    onWeatherClick: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {

        
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Overview Statistics
                item {
                    OverviewSection(
                        dashboard = dashboard,
                        onNavigateToTanks = onNavigateToTanks,
                        onWeatherClick = onWeatherClick
                    )
                }

                // Recent Alerts
                item {
                    AlertsSection(
                        alerts = dashboard.recentAlerts,
                        onAlertClick = onNavigateToTankDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(
    dashboard: Dashboard,
    onNavigateToTanks: () -> Unit,
    onWeatherClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Tanks
            NewStatBox(
                iconVector = Icons.Filled.SetMeal, 
                value = dashboard.activeTanks.toString(),
                label = "Active Tanks",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTanks
            )

            // Total Volume
            NewStatBox(
                iconVector = Icons.Default.WaterDrop,
                value = "${dashboard.totalVolume}m³",
                label = "Total Volume",
                modifier = Modifier.weight(1f)
            )

            // Avg Temp
            NewStatBox(
                iconVector = Icons.Default.Thermostat,
                value = "${dashboard.avgTemperature.toInt()}°C",
                label = "Avg Temp",
                modifier = Modifier.weight(1f)
            )
        }

        // Weather Card
        WeatherCard(
            onClick = onWeatherClick
        )
    }
}

@Composable
private fun NewStatBox(
    iconVector: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = label,
                tint = Color(0xFF366D96), // Aqua Blue
                modifier = Modifier.size(32.dp)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1D2A)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: List<Alert>,
    onAlertClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Alerts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (alerts.isNotEmpty()) {
                Text(
                    text = "${alerts.size} alert${if (alerts.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (alerts.isEmpty()) {
            NoAlertsCard()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alerts.forEach { alert ->
                    AlertCard(
                        alert = alert,
                        onClick = { onAlertClick(alert.tankId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoAlertsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "✓ All Systems Normal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No alerts at this time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun DashboardScreenPreview() {
    AquaSenseTheme {
        SuccessContent(
            dashboard = Dashboard(
                totalTanks = 5,
                activeTanks = 5,
                totalVolume = 1250.0,
                avgTemperature = 24.5,
                totalFish = 250,
                tanksNeedingAttention = 2,
                recentAlerts = listOf(
                    Alert(
                        tankId = "1",
                        tankName = "Main Pond",
                        type = AlertType.WARNING,
                        message = "pH level is out of range (9.2)"
                    ),
                    Alert(
                        tankId = "2",
                        tankName = "Breeding Tank",
                        type = AlertType.CRITICAL,
                        message = "High ammonia levels detected (0.8 mg/L)"
                    )
                )
            ),
            isRefreshing = false,
            onRefresh = {},
            onNavigateToTanks = {},
            onNavigateToAddTank = {},
            onNavigateToTankDetail = {},
            onWeatherClick = {},
            paddingValues = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenNoAlertsPreview() {
    AquaSenseTheme {
        SuccessContent(
            dashboard = Dashboard(
                totalTanks = 3,
                activeTanks = 3,
                totalVolume = 800.0,
                avgTemperature = 22.0,
                totalFish = 120,
                tanksNeedingAttention = 0,
                recentAlerts = emptyList()
            ),
            isRefreshing = false,
            onRefresh = {},
            onNavigateToTanks = {},
            onNavigateToAddTank = {},
            onNavigateToTankDetail = {},
            onWeatherClick = {},
            paddingValues = PaddingValues(0.dp)
        )
    }
}

