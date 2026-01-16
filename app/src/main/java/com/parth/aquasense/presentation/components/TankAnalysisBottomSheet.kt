package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.HealthCategory
import com.parth.aquasense.domain.model.TankAnalysis
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Bottom Sheet component for displaying AI-powered tank analysis
 *
 * Shows:
 * - Health score with color-coded progress indicator
 * - Water quality analysis text
 * - List of recommendations
 * - List of warnings (if any)
 * - Share and dismiss actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankAnalysisBottomSheet(
    analysis: TankAnalysis,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with tank name and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Analysis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            // Tank name
            Text(
                text = analysis.tankName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Health Score Card
            HealthScoreCard(
                healthScore = analysis.healthScore,
                healthCategory = analysis.healthCategory
            )

            // Analysis Text
            if (analysis.analysis.isNotBlank()) {
                AnalysisTextCard(text = analysis.analysis)
            }

            // Warnings (if any)
            if (analysis.hasWarnings) {
                WarningsCard(warnings = analysis.warnings)
            }

            // Recommendations
            if (analysis.recommendations.isNotEmpty()) {
                RecommendationsCard(recommendations = analysis.recommendations)
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Share")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

/**
 * Health score card with color-coded progress indicator
 */
@Composable
private fun HealthScoreCard(
    healthScore: Int,
    healthCategory: HealthCategory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Health Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$healthScore/100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getHealthScoreColor(healthCategory)
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { healthScore / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = getHealthScoreColor(healthCategory)
            )

            // Health category badge
            Text(
                text = healthCategory.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = getHealthScoreColor(healthCategory),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Get color based on health category
 */
@Composable
private fun getHealthScoreColor(category: HealthCategory) = when (category) {
    HealthCategory.EXCELLENT -> MaterialTheme.colorScheme.primary
    HealthCategory.GOOD -> MaterialTheme.colorScheme.tertiary
    HealthCategory.FAIR -> MaterialTheme.colorScheme.secondary
    HealthCategory.POOR -> MaterialTheme.colorScheme.error
}

/**
 * Analysis text card
 */
@Composable
private fun AnalysisTextCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Water Quality Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FormattedText(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Warnings card (shown only if warnings exist)
 */
@Composable
private fun WarningsCard(
    warnings: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Warnings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                warnings.forEach { warning ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        FormattedText(
                            text = warning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recommendations card
 */
@Composable
private fun RecommendationsCard(
    recommendations: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendations.forEach { recommendation ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FormattedText(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TankAnalysisBottomSheetPreview() {
    AquaSenseTheme {
        TankAnalysisBottomSheet(
            analysis = TankAnalysis(
                tankId = "1",
                tankName = "Main Pond",
                healthScore = 85,
                analysis = """
                    # Water Quality Status: Good

                    All parameters are within **safe ranges** for Tilapia:

                    - pH: 7.2 (optimal)
                    - Temperature: 28°C (ideal)
                    - Dissolved Oxygen: 6.5 mg/L (good)
                    - Ammonia (NH3): 0.02 ppm (safe)

                    The tank shows **healthy biological balance** with stable parameters.
                """.trimIndent(),
                recommendations = listOf(
                    "Monitor **ammonia levels** closely over the next 48 hours",
                    "Consider increasing aeration during **feeding times**",
                    "Schedule a partial water change within the next week"
                ),
                warnings = listOf(
                    "Dissolved oxygen is approaching **lower threshold**",
                    "Temperature variation detected in the last **24 hours**"
                ),
                timestamp = "2025-12-26T10:30:00Z"
            ),
            onDismiss = {},
            onShare = {}
        )
    }
}
