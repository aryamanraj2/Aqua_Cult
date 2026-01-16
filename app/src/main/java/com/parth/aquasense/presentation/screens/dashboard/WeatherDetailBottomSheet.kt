package com.parth.aquasense.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherDetailBottomSheet(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7)) // Light Gray Background like iOS
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(Color.White, CircleShape)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF366D96)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Weather Info Card
        MainWeatherInfoCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Water Surface Impact Card
        WaterSurfaceImpactCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Aquaculture Impact Card
        DailyAquacultureImpactCard()

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun MainWeatherInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color(0xFF366D96),
                    modifier = Modifier.size(64.dp)
                )
                Column {
                    Text(
                        text = "22°C",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1D2A)
                    )
                    Text(
                        text = "Partly Cloudy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Wind: 12km/h",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0D1D2A)
                )
                Text(
                    text = "Humidity: 68%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0D1D2A)
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.3f))
        
        Text(
            text = "Optimal for aquaculture: Excellent",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun WaterSurfaceImpactCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Water Surface Impact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1D2A),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Row 1
                Row(modifier = Modifier.fillMaxWidth()) {
                    ImpactItem(
                        icon = Icons.Default.Waves,
                        iconColor = Color(0xFF366D96),
                        label = "Surface Mixing",
                        value = "Gentle",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactItem(
                        icon = null, // Text based for Oxygen Transfer in design usually but using consistent layout
                        label = "Oxygen Transfer",
                        value = "Enhanced",
                        alignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    ImpactItem(
                        icon = Icons.Default.Thermostat,
                        iconColor = Color(0xFFFFA726), // Orange/Yellow
                        label = "Heat Exchange",
                        value = "Stable",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactItem(
                        icon = null,
                        label = "Water Quality",
                        value = "Stable",
                        alignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactItem(
    icon: ImageVector?,
    iconColor: Color = Color.Black,
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D1D2A),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun DailyAquacultureImpactCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Daily Aquaculture Impact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1D2A),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                DailyImpactRow(
                    icon = Icons.Default.WbSunny, // Morning icon placeholder
                    iconColor = Color(0xFF66BB6A), // Green
                    period = "Morning",
                    desc = "Stable conditions for feeding",
                    status = "Feeding optimal",
                    statusColor = Color(0xFF66BB6A) // Green
                )
                DailyImpactRow(
                    icon = Icons.Default.WbSunny,
                    iconColor = Color(0xFF66BB6A), // Green
                    period = "Midday",
                    desc = "Normal operations",
                    status = "Good conditions",
                    statusColor = Color(0xFF66BB6A)
                )
                DailyImpactRow(
                    icon = Icons.Default.WbTwilight,
                    iconColor = Color(0xFFFFA726), // Orange/Yellow
                    period = "Evening",
                    desc = "Check oxygen levels",
                    status = "Monitor closely",
                    statusColor = Color(0xFFFFA726)
                )
                DailyImpactRow(
                    icon = Icons.Default.DarkMode,
                    iconColor = Color(0xFF366D96), // Blue
                    period = "Night",
                    desc = "Low activity expected",
                    status = "Stable period",
                    statusColor = Color(0xFF366D96)
                )
            }
        }
    }
}

@Composable
private fun DailyImpactRow(
    icon: ImageVector,
    iconColor: Color,
    period: String,
    desc: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = period,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1D2A)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}
