package com.parth.aquasense.presentation.screens.productdetail

import com.parth.aquasense.domain.model.Product

/**
 * UI State for the Product Detail screen.
 */
sealed class ProductDetailUiState {
    /**
     * Initial loading state when fetching product details.
     */
    data object Loading : ProductDetailUiState()

    /**
     * Success state with product loaded.
     *
     * @param product The product being displayed
     * @param quantity Current quantity selected (for adding to cart)
     * @param isRefreshing True when pull-to-refresh is active
     */
    data class Success(
        val product: Product,
        val quantity: Int = 1,
        val isRefreshing: Boolean = false
    ) : ProductDetailUiState()

    /**
     * Error state when product fetching fails.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : ProductDetailUiState()
}
