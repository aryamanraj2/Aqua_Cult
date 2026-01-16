package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.EnrichedOrderItem
import com.parth.aquasense.domain.model.Order
import java.util.Locale

/**
 * Reusable card component for displaying order information in lists.
 *
 * Shows order ID, status, items (with product names if available), date, and total amount.
 * Status is color-coded for quick visual identification.
 *
 * @param order The order to display
 * @param enrichedItems Optional enriched items with product names (null if still loading)
 * @param onClick Callback when card is clicked
 * @param modifier Optional modifier for the card
 */
@Composable
fun OrderCard(
    order: Order,
    enrichedItems: List<EnrichedOrderItem>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Order header with ID and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Status badge with color coding
                val statusColor = when (order.status.lowercase(Locale.getDefault())) {
                    "pending" -> MaterialTheme.colorScheme.secondary
                    "confirmed" -> MaterialTheme.colorScheme.tertiary
                    "shipped" -> MaterialTheme.colorScheme.primary
                    "delivered" -> MaterialTheme.colorScheme.primaryContainer
                    "cancelled" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surface
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = order.status.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show product names if enriched data is available
            if (enrichedItems != null && enrichedItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Show first 3 items
                    enrichedItems.take(3).forEach { item ->
                        Text(
                            text = "• ${item.productName} x${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Show "+N more items" if there are more than 3
                    if (enrichedItems.size > 3) {
                        Text(
                            text = "  +${enrichedItems.size - 3} more items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            } else {
                // Fallback: just show item count
                Text(
                    text = "${order.items.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Order date
            Text(
                text = "Placed: ${order.createdAt.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Total amount
            Text(
                text = "Total: ₹${order.totalAmount}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
