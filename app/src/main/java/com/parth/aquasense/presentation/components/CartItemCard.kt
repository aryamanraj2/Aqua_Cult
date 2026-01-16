package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.CartItem

/**
 * Reusable card component for displaying cart items.
 *
 * Shows product name, price, quantity controls, and subtotal.
 * Allows incrementing/decrementing quantity and removing items.
 *
 * @param cartItem The cart item to display
 * @param onQuantityChange Callback when quantity is changed
 * @param onRemove Callback when item is removed
 * @param modifier Optional modifier for the card
 */
@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "₹${cartItem.product.price}/${cartItem.product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: ₹${cartItem.subtotal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Quantity controls and delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Decrease quantity button
                IconButton(
                    onClick = { onQuantityChange(cartItem.quantity - 1) },
                    enabled = cartItem.quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                }

                // Quantity display
                Text(
                    text = cartItem.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Increase quantity button
                IconButton(
                    onClick = { onQuantityChange(cartItem.quantity + 1) },
                    enabled = cartItem.quantity < cartItem.product.stockQuantity
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                }

                // Remove item button
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove from cart",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
