package com.parth.aquasense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weather Icon & Temp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Partly Cloudy",
                    tint = Color(0xFF547494), // Muted Blue
                    modifier = Modifier.size(48.dp)
                )
                
                Column {
                    Text(
                        text = "22°",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1D2A) // Dark text
                    )
                    Text(
                        text = "Partly Cloudy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // Stats (Humidity & Rain)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Humidity
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Humidity",
                        tint = Color(0xFF547494),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "68%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0D1D2A)
                    )
                    Text(
                        text = "Humidity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Rain
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Rain",
                        tint = Color(0xFF366D96), // Aqua Blue
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "25%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0D1D2A)
                    )
                    Text(
                        text = "Rain",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }
        }

    }
}
