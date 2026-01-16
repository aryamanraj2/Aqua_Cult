package com.parth.aquasense.domain.model

import com.parth.aquasense.data.repository.ProductRepository
import com.parth.aquasense.domain.util.Result

/**
 * Domain model representing an order placed by the user.
 */
data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val deliveryAddress: String?,
    val paymentMethod: String?,
    val paymentStatus: String,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Represents an item in an order.
 * Note: Product name is fetched separately and may be null initially.
 */
data class OrderItem(
    val productId: String,
    val productName: String? = null,  // Fetched separately for display
    val quantity: Int,
    val price: Double
)

/**
 * Order item enriched with product name for display purposes.
 * This is used in Order History to show actual product names instead of just IDs.
 */
data class EnrichedOrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
) {
    companion object {
        /**
         * Creates an EnrichedOrderItem by fetching the product name from the repository.
         */
        suspend fun from(orderItem: OrderItem, productRepository: ProductRepository): EnrichedOrderItem {
            val productName = when (val result = productRepository.getProductById(orderItem.productId)) {
                is Result.Success -> result.data.name
                is Result.Error -> "Unknown Product"
            }
            return EnrichedOrderItem(
                productId = orderItem.productId,
                productName = productName,
                quantity = orderItem.quantity,
                price = orderItem.price,
                subtotal = orderItem.price * orderItem.quantity
            )
        }
    }
}

/**
 * Represents an item in the shopping cart (local state only).
 */
data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}
