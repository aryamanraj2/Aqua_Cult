package com.parth.aquasense.presentation.screens.marketplace

import com.parth.aquasense.domain.model.Product

/**
 * UI State for the Marketplace screen.
 *
 * Follows the established pattern of Loading/Success/Error states.
 */
sealed class MarketplaceUiState {
    /**
     * Initial loading state when fetching products and categories.
     */
    data object Loading : MarketplaceUiState()

    /**
     * Success state with products and categories loaded.
     *
     * @param products List of products (filtered by category if selected)
     * @param categories List of all available categories
     * @param selectedCategory Currently selected category filter (null = all products)
     * @param isRefreshing True when pull-to-refresh is active
     */
    data class Success(
        val products: List<Product>,
        val categories: List<String>,
        val selectedCategory: String? = null,
        val isRefreshing: Boolean = false
    ) : MarketplaceUiState()

    /**
     * Error state when product fetching fails.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : MarketplaceUiState()
}
