package com.parth.aquasense.presentation.screens.orderhistory

import com.parth.aquasense.domain.model.EnrichedOrderItem
import com.parth.aquasense.domain.model.Order

/**
 * UI State for the Order History screen.
 */
sealed class OrderHistoryUiState {
    /**
     * Initial loading state when fetching orders.
     */
    data object Loading : OrderHistoryUiState()

    /**
     * Success state with orders loaded.
     *
     * @param orders List of orders
     * @param enrichedItems Map of order ID to enriched items (with product names)
     * @param isRefreshing True when pull-to-refresh is active
     */
    data class Success(
        val orders: List<Order>,
        val enrichedItems: Map<String, List<EnrichedOrderItem>> = emptyMap(),
        val isRefreshing: Boolean = false
    ) : OrderHistoryUiState()

    /**
     * Error state when order fetching fails.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : OrderHistoryUiState()
}
