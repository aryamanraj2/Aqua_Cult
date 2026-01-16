package com.parth.aquasense.presentation.screens.productdetail

import androidx.lifecycle.SavedStateHandle
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
 * ViewModel for the Product Detail screen.
 *
 * Manages product fetching and quantity selection for adding to cart.
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"]) {
        "Product ID is required"
    }

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    /**
     * Load product details from the repository.
     */
    fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading

            when (val result = productRepository.getProductById(productId)) {
                is Result.Success -> {
                    _uiState.value = ProductDetailUiState.Success(product = result.data)
                }
                is Result.Error -> {
                    _uiState.value = ProductDetailUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Update the quantity to add to cart.
     *
     * @param quantity New quantity (will be clamped between 1 and stock quantity)
     */
    fun updateQuantity(quantity: Int) {
        val currentState = _uiState.value
        if (currentState is ProductDetailUiState.Success) {
            _uiState.value = currentState.copy(
                quantity = quantity.coerceIn(1, currentState.product.stockQuantity)
            )
        }
    }

    /**
     * Get the current product and quantity for adding to cart.
     * This is called by the screen when user clicks "Add to Cart".
     */
    fun getCurrentProductAndQuantity(): Pair<com.parth.aquasense.domain.model.Product, Int>? {
        val currentState = _uiState.value
        return if (currentState is ProductDetailUiState.Success) {
            Pair(currentState.product, currentState.quantity)
        } else {
            null
        }
    }
}
