package com.parth.aquasense.presentation.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.CartItem
import com.parth.aquasense.domain.util.Result
import com.parth.aquasense.presentation.components.CartItemCard
import kotlinx.coroutines.launch

/**
 * Cart screen showing items to be purchased.
 *
 * Features:
 * - List of cart items with quantity controls
 * - Total amount calculation
 * - Place order button
 * - Empty cart state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onOrderPlaced: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart (${cartItems.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartState(
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CartContent(
                cartItems = cartItems,
                totalAmount = totalAmount,
                isPlacingOrder = isPlacingOrder,
                onQuantityChange = { productId, quantity ->
                    viewModel.updateQuantity(productId, quantity)
                },
                onRemove = { productId ->
                    viewModel.removeFromCart(productId)
                },
                onPlaceOrder = {
                    scope.launch {
                        when (viewModel.placeOrder()) {
                            is Result.Success -> onOrderPlaced()
                            is Result.Error -> {
                                // Error handling - could show a snackbar
                            }
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun CartContent(
    cartItems: List<CartItem>,
    totalAmount: Double,
    isPlacingOrder: Boolean,
    onQuantityChange: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Cart items list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartItems, key = { it.product.id }) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onQuantityChange = { quantity ->
                        onQuantityChange(cartItem.product.id, quantity)
                    },
                    onRemove = { onRemove(cartItem.product.id) }
                )
            }
        }

        // Checkout section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹$totalAmount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPlaceOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isPlacingOrder
                ) {
                    if (isPlacingOrder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Place Order",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCartState(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add products to your cart to place an order",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onNavigateBack) {
            Text("Browse Products")
        }
    }
}
