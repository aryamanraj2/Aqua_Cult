package com.parth.aquasense.presentation.screens.orderhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.EnrichedOrderItem
import com.parth.aquasense.domain.model.Order
import com.parth.aquasense.presentation.components.OrderCard

/**
 * Order History screen showing all user orders.
 *
 * Features:
 * - List of orders with enriched product names
 * - Pull-to-refresh
 * - Order status color coding
 * - Empty state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateToOrderDetail: (String) -> Unit = {},
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = (uiState as? OrderHistoryUiState.Success)?.isRefreshing ?: false,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is OrderHistoryUiState.Loading -> LoadingState()
                is OrderHistoryUiState.Success -> {
                    if (state.orders.isEmpty()) {
                        EmptyOrdersState()
                    } else {
                        OrderHistoryContent(
                            orders = state.orders,
                            enrichedItems = state.enrichedItems,
                            onOrderClick = onNavigateToOrderDetail
                        )
                    }
                }
                is OrderHistoryUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadOrders() }
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryContent(
    orders: List<Order>,
    enrichedItems: Map<String, List<EnrichedOrderItem>>,
    onOrderClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders, key = { it.id }) { order ->
            OrderCard(
                order = order,
                enrichedItems = enrichedItems[order.id],
                onClick = { onOrderClick(order.id) }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyOrdersState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No orders yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your order history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        FilledTonalButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Retry")
        }
    }
}
