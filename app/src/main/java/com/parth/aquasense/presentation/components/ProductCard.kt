package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.Product
import java.util.Locale

/**
 * Reusable card component for displaying product information in lists.
 *
 * Follows Material Design 3 guidelines with proper spacing and typography.
 * Shows product name, category, description, price, and stock status.
 *
 * @param product The product to display
 * @param onClick Callback when card is clicked
 * @param modifier Optional modifier for the card
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = product.category.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Description (if available)
            if (product.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Text(
                    text = "₹${product.price}/${product.unit}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Stock indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (product.stockQuantity > 0)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (product.stockQuantity > 0)
                            "${product.stockQuantity} in stock"
                        else
                            "Out of stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stockQuantity > 0)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
