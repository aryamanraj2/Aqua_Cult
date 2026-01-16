package com.parth.aquasense.presentation.screens.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.ProductRepository
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Marketplace screen.
 *
 * Manages product fetching, category filtering, and pull-to-refresh.
 */
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarketplaceUiState>(MarketplaceUiState.Loading)
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    /**
     * Load products and categories from the repository.
     *
     * @param category Optional category filter (null loads all products)
     */
    fun loadProducts(category: String? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Only set loading state if we don't have data yet
            if (currentState !is MarketplaceUiState.Success) {
                _uiState.value = MarketplaceUiState.Loading
            }

            // Fetch categories only if we don't have them
            val categories = if (currentState is MarketplaceUiState.Success) {
                currentState.categories
            } else {
                when (val result = productRepository.getCategories()) {
                    is Result.Success -> result.data
                    is Result.Error -> {
                        _uiState.value = MarketplaceUiState.Error(result.message)
                        return@launch
                    }
                }
            }

            // Fetch products
            when (val result = productRepository.getProducts(category)) {
                is Result.Success -> {
                    _uiState.value = MarketplaceUiState.Success(
                        products = result.data,
                        categories = categories,
                        selectedCategory = category,
                        isRefreshing = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = MarketplaceUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Refresh products while maintaining the current category filter.
     */
    fun refresh() {
        val currentState = _uiState.value
        if (currentState !is MarketplaceUiState.Success) {
            loadProducts()
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isRefreshing = true)

            when (val result = productRepository.getProducts(currentState.selectedCategory)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        products = result.data,
                        isRefreshing = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(isRefreshing = false)
                }
            }
        }
    }

    /**
     * Filter products by category.
     *
     * @param category Category to filter by (null for all products)
     */
    fun filterByCategory(category: String?) {
        loadProducts(category)
    }
}
