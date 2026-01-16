package com.parth.aquasense.presentation.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.OrderRepository
import com.parth.aquasense.domain.model.CartItem
import com.parth.aquasense.domain.model.Order
import com.parth.aquasense.domain.model.OrderItem
import com.parth.aquasense.domain.model.Product
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Cart screen.
 *
 * Manages cart state (in-memory only, no persistence).
 * Handles adding/removing items, quantity updates, and order placement.
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    /**
     * Total amount of all items in cart.
     * Automatically updates when cart items change.
     */
    val totalAmount: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    /**
     * Add a product to the cart.
     * If product already exists, increases quantity.
     *
     * @param product Product to add
     * @param quantity Quantity to add
     */
    fun addToCart(product: Product, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingIndex != -1) {
            // Update existing item quantity
            currentItems[existingIndex] = currentItems[existingIndex].copy(
                quantity = currentItems[existingIndex].quantity + quantity
            )
        } else {
            // Add new item
            currentItems.add(CartItem(product, quantity))
        }

        _cartItems.value = currentItems
    }

    /**
     * Update quantity of a cart item.
     * If quantity becomes 0 or negative, removes the item.
     *
     * @param productId ID of the product
     * @param quantity New quantity
     */
    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        _cartItems.value = _cartItems.value.map { item ->
            if (item.product.id == productId) {
                item.copy(quantity = quantity.coerceIn(1, item.product.stockQuantity))
            } else {
                item
            }
        }
    }

    /**
     * Remove an item from the cart.
     *
     * @param productId ID of the product to remove
     */
    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    /**
     * Clear all items from the cart.
     */
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    /**
     * Place an order with all items in the cart.
     * Clears cart on success.
     *
     * @param deliveryAddress Optional delivery address
     * @param paymentMethod Optional payment method
     * @param notes Optional order notes
     * @return Result containing the created order or error
     */
    suspend fun placeOrder(
        deliveryAddress: String? = null,
        paymentMethod: String? = null,
        notes: String? = null
    ): Result<Order> {
        _isPlacingOrder.value = true

        val items = _cartItems.value.map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                price = cartItem.product.price
            )
        }

        val result = orderRepository.createOrder(
            items = items,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod,
            notes = notes
        )

        _isPlacingOrder.value = false

        if (result is Result.Success) {
            clearCart()
        }

        return result
    }
}
