package com.parth.aquasense.presentation.screens.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.OrderRepository
import com.parth.aquasense.data.repository.ProductRepository
import com.parth.aquasense.domain.model.EnrichedOrderItem
import com.parth.aquasense.domain.model.Order
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Order History screen.
 *
 * Manages order fetching and enriches order items with product names.
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load orders from the repository and enrich with product names.
     */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState.Loading

            when (val result = orderRepository.getOrders()) {
                is Result.Success -> {
                    val orders = result.data
                    val enrichedItems = enrichOrderItems(orders)
                    _uiState.value = OrderHistoryUiState.Success(
                        orders = orders,
                        enrichedItems = enrichedItems
                    )
                }
                is Result.Error -> {
                    _uiState.value = OrderHistoryUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Enrich order items with product names by fetching product details.
     *
     * @param orders List of orders to enrich
     * @return Map of order ID to list of enriched items
     */
    private suspend fun enrichOrderItems(orders: List<Order>): Map<String, List<EnrichedOrderItem>> {
        val enrichedMap = mutableMapOf<String, List<EnrichedOrderItem>>()

        orders.forEach { order ->
            val enrichedItems = order.items.map { item ->
                EnrichedOrderItem.from(item, productRepository)
            }
            enrichedMap[order.id] = enrichedItems
        }

        return enrichedMap
    }

    /**
     * Refresh orders while maintaining enriched data.
     */
    fun refresh() {
        val currentState = _uiState.value
        if (currentState !is OrderHistoryUiState.Success) {
            loadOrders()
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isRefreshing = true)

            when (val result = orderRepository.getOrders()) {
                is Result.Success -> {
                    val orders = result.data
                    val enrichedItems = enrichOrderItems(orders)
                    _uiState.value = currentState.copy(
                        orders = orders,
                        enrichedItems = enrichedItems,
                        isRefreshing = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(isRefreshing = false)
                }
            }
        }
    }
}
