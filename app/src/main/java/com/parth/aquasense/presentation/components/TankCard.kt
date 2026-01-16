package com.parth.aquasense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.TankStatus

/**
 * Tank Card - Displays tank information in a card
 *
 * Shows:
 * - Tank name and location
 * - Species list
 * - Current stock and capacity
 * - Stock percentage with visual indicator
 * - Status badge
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankCard(
    tank: Tank,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://images.unsplash.com/photo-1522069169874-c58ec4b76be5") // Placeholder ocean/water image
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Decorative Icon (Top Right)
            Box(
                 modifier = Modifier
                     .align(Alignment.TopEnd)
                     .padding(16.dp)
            ) {
                 Surface(
                     color = Color.White.copy(alpha = 0.2f),
                     shape = MaterialTheme.shapes.medium,
                     modifier = Modifier.size(40.dp)
                 ) {
                     Box(contentAlignment = Alignment.Center) {
                         Icon(
                             imageVector = Icons.Default.Info, // Placeholder for "Brain" icon
                             contentDescription = null,
                             tint = Color.White,
                             modifier = Modifier.size(24.dp)
                         )
                     }
                 }
            }

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left Side: Name and Location
                    Column {
                        Text(
                            text = tank.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = tank.location ?: "Unknown Location",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Right Side: Status Label and Value
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Current Stage", // Using static label as per design
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = tank.status.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TankCardPreview() {
    val sampleTank = Tank(
        id = "1",
        userId = "user1",
        name = "Atlantic Salmon",
        capacity = 1000.0,
        currentStock = 200,
        species = listOf("Salmon"),
        location = "Zone A - North",
        status = TankStatus.ACTIVE,
        createdAt = "",
        updatedAt = ""
    )

    MaterialTheme {
        TankCard(
            tank = sampleTank,
            onClick = {}
        )
    }
}
