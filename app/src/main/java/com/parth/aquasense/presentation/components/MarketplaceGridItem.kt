package com.parth.aquasense.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parth.aquasense.domain.model.Product
import com.parth.aquasense.ui.theme.AquaBlue

/**
 * Grid item for the Marketplace screen.
 * Displays product info in a modern card layout.
 */
@Composable
fun MarketplaceGridItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f), // Adjust aspect ratio to match design
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Placeholder Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(com.parth.aquasense.ui.theme.BackgroundCyan.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Dynamic icon based on category
                val icon = when {
                    product.category.contains("feed", ignoreCase = true) || 
                    product.category.contains("food", ignoreCase = true) -> Icons.Rounded.Restaurant
                    
                    product.category.contains("medicine", ignoreCase = true) || 
                    product.category.contains("treatment", ignoreCase = true) -> Icons.Rounded.MedicalServices
                    
                    product.category.contains("equipment", ignoreCase = true) || 
                    product.category.contains("device", ignoreCase = true) -> Icons.Rounded.Settings
                    
                    product.category.contains("iot", ignoreCase = true) || 
                    product.category.contains("sensor", ignoreCase = true) -> Icons.Rounded.Sensors
                    
                    else -> Icons.Rounded.Category
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = product.category,
                    tint = AquaBlue,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Content
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rating Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107), // Amber/Gold for star
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "4.5", // Static rating as per plan
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹${product.price}0", // Ensure 2 decimal places roughly
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = AquaBlue
                    )
                )
            }
        }
    }
}
