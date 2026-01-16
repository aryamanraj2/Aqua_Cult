package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.WaterQuality


enum class MetricStatus {
    GOOD, MODERATE, DANGEROUS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterQualityGrid(
    waterQuality: WaterQuality,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tank Conditions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val itemModifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.48f) // Approx half width with spacing

            // pH Level
            MetricCard(
                label = "pH Level",
                value = waterQuality.ph?.let { String.format("%.1f", it) } ?: "N/A",
                unit = "",
                icon = Icons.Default.Opacity, // Droplet/Liquid
                status = getPhStatus(waterQuality.ph),
                modifier = itemModifier
            )

            // Water Temp
            MetricCard(
                label = "Water Temp",
                value = waterQuality.temperature?.let { String.format("%.1f", it) } ?: "N/A",
                unit = "°C",
                icon = Icons.Default.DeviceThermostat,
                status = getTempStatus(waterQuality.temperature),
                modifier = itemModifier
            )

            // Dissolved O2
            MetricCard(
                label = "Dissolved O₂",
                value = waterQuality.dissolvedOxygen?.let { String.format("%.1f", it) } ?: "N/A",
                unit = "mg/L",
                icon = Icons.Default.Air, // Close enough to bubbles/air
                status = getO2Status(waterQuality.dissolvedOxygen),
                modifier = itemModifier
            )

            // Ammonia
            MetricCard(
                label = "Ammonia",
                value = waterQuality.ammonia?.let { String.format("%.2f", it) } ?: "N/A",
                unit = "mg/L",
                icon = Icons.Default.Eco, // Leaf/Organic
                status = getAmmoniaStatus(waterQuality.ammonia),
                modifier = itemModifier
            )

            // Salinity (Optional)
            if (waterQuality.salinity != null) {
                MetricCard(
                    label = "Salinity",
                    value = String.format("%.1f", waterQuality.salinity),
                    unit = "ppt",
                    icon = Icons.Default.Waves,
                    status = MetricStatus.GOOD, // Placeholder logic
                    modifier = itemModifier
                )
            }

            // Turbidity (Optional)
            if (waterQuality.turbidity != null) {
                MetricCard(
                    label = "Turbidity",
                    value = String.format("%.1f", waterQuality.turbidity),
                    unit = "NTU",
                    icon = Icons.Default.Visibility, // Eye
                    status = MetricStatus.GOOD, // Placeholder logic
                    modifier = itemModifier
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    status: MetricStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, iconColor, statusText) = when (status) {
        MetricStatus.GOOD -> Quad(
            Color(0xFFE8F5E9), // Light Green
            Color(0xFF2E7D32), // Dark Green
            Color(0xFF4CAF50), // Icon Green
            "Good" // or Optimal
        )
        MetricStatus.MODERATE -> Quad(
            Color(0xFFFFF8E1), // Light Yellow/Amber
            Color(0xFFF57C00), // Dark Orange
            Color(0xFFFFB74D), // Icon Orange
            "Moderate"
        )
        MetricStatus.DANGEROUS -> Quad(
            Color(0xFFFFEBEE), // Light Red
            Color(0xFFC62828), // Dark Red
            Color(0xFFEF5350), // Icon Red
            "Danger"
        )
    }

    Card(
        modifier = modifier.height(130.dp), // Fixed height for uniformity
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Icon + Status Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Value + Label
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall, // existing code uses titleLarge?
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Value is usually black/dark
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper data class for tuple return
data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// --- Default Logic Placeholders ---
// You should adjust these ranges based on specific aquaculture requirements!

fun getPhStatus(ph: Double?): MetricStatus {
    if (ph == null) return MetricStatus.MODERATE
    return when {
        ph in 6.5..8.5 -> MetricStatus.GOOD
        ph in 6.0..9.0 -> MetricStatus.MODERATE
        else -> MetricStatus.DANGEROUS
    }
}

fun getTempStatus(temp: Double?): MetricStatus {
    if (temp == null) return MetricStatus.MODERATE
    return when {
        temp in 20.0..30.0 -> MetricStatus.GOOD
        temp in 15.0..35.0 -> MetricStatus.MODERATE
        else -> MetricStatus.DANGEROUS
    }
}

fun getO2Status(o2: Double?): MetricStatus {
    if (o2 == null) return MetricStatus.MODERATE
    return when {
        o2 >= 5.0 -> MetricStatus.GOOD
        o2 >= 3.0 -> MetricStatus.MODERATE
        else -> MetricStatus.DANGEROUS
    }
}

fun getAmmoniaStatus(ammonia: Double?): MetricStatus {
    if (ammonia == null) return MetricStatus.MODERATE
    return when {
        ammonia < 0.05 -> MetricStatus.GOOD
        ammonia < 0.1 -> MetricStatus.MODERATE
        else -> MetricStatus.DANGEROUS
    }
}
