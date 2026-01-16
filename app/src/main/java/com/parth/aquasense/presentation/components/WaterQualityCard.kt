package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.WaterQuality
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Card component displaying water quality parameters
 * Shows all water quality readings with visual indicators for safe/warning status
 */
@Composable
fun WaterQualityCard(
    waterQuality: WaterQuality,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (waterQuality.isSafe) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Water Quality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (waterQuality.isSafe) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = if (waterQuality.isSafe) "Safe" else "Warning",
                        tint = if (waterQuality.isSafe) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = if (waterQuality.isSafe) "Safe" else "Warning",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (waterQuality.isSafe) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Warning messages
            if (waterQuality.warnings.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    waterQuality.warnings.forEach { warning ->
                        Text(
                            text = "⚠ $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Parameters Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WaterQualityParameter(
                    name = "pH",
                    value = String.format("%.1f", waterQuality.ph)
                )
                WaterQualityParameter(
                    name = "Temperature",
                    value = String.format("%.1f°C", waterQuality.temperature)
                )
                WaterQualityParameter(
                    name = "Dissolved Oxygen",
                    value = String.format("%.2f mg/L", waterQuality.dissolvedOxygen)
                )
                WaterQualityParameter(
                    name = "Ammonia",
                    value = String.format("%.2f mg/L", waterQuality.ammonia)
                )
                WaterQualityParameter(
                    name = "Nitrite",
                    value = String.format("%.2f mg/L", waterQuality.nitrite)
                )
                WaterQualityParameter(
                    name = "Nitrate",
                    value = String.format("%.2f mg/L", waterQuality.nitrate)
                )
                waterQuality.salinity?.let { salinity ->
                    WaterQualityParameter(
                        name = "Salinity",
                        value = String.format("%.2f ppt", salinity)
                    )
                }
                waterQuality.turbidity?.let { turbidity ->
                    WaterQualityParameter(
                        name = "Turbidity",
                        value = String.format("%.2f NTU", turbidity)
                    )
                }
            }

            // Recorded timestamp
            Text(
                text = "Recorded: ${waterQuality.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Single parameter row showing name and value
 */
@Composable
private fun WaterQualityParameter(
    name: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WaterQualityCardSafePreview() {
    AquaSenseTheme {
        WaterQualityCard(
            waterQuality = WaterQuality(
                id = "1",
                tankId = "tank1",
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
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WaterQualityCardWarningPreview() {
    AquaSenseTheme {
        WaterQualityCard(
            waterQuality = WaterQuality(
                id = "2",
                tankId = "tank1",
                ph = 9.5,
                temperature = 32.0,
                dissolvedOxygen = 3.0,
                ammonia = 1.5,
                nitrite = 0.05,
                nitrate = 10.0,
                salinity = null,
                turbidity = null,
                createdAt = "2025-12-24T10:30:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
