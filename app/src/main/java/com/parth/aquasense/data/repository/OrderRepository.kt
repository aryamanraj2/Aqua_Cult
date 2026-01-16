package com.parth.aquasense.data.repository

import com.parth.aquasense.data.remote.api.OrderApi
import com.parth.aquasense.data.remote.dto.OrderCreateRequest
import com.parth.aquasense.data.remote.dto.OrderItemCreateDto
import com.parth.aquasense.data.remote.dto.toDomain
import com.parth.aquasense.domain.model.Order
import com.parth.aquasense.domain.model.OrderItem
import com.parth.aquasense.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for order-related data operations.
 *
 * Handles creating orders, fetching order history, and order management.
 * All operations are performed on the IO dispatcher for better performance.
 */
@Singleton
class OrderRepository @Inject constructor(
    private val orderApi: OrderApi
) {

    /**
     * Create a new order.
     *
     * @param items List of order items with product IDs and quantities
     * @param deliveryAddress Optional delivery address
     * @param paymentMethod Optional payment method
     * @param notes Optional order notes
     * @return Result containing the created order or error message
     */
    suspend fun createOrder(
        items: List<OrderItem>,
        deliveryAddress: String? = null,
        paymentMethod: String? = null,
        notes: String? = null
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val request = OrderCreateRequest(
                items = items.map {
                    OrderItemCreateDto(productId = it.productId, quantity = it.quantity)
                },
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                notes = notes
            )
            val response = orderApi.createOrder(request)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create order")
        }
    }

    /**
     * Get all orders for the current user.
     *
     * @return Result containing list of orders or error message
     */
    suspend fun getOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getOrders()
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch orders")
        }
    }

    /**
     * Get a specific order by ID.
     *
     * @param orderId The UUID of the order
     * @return Result containing the order or error message
     */
    suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getOrderById(orderId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch order")
        }
    }

    /**
     * Cancel an order.
     * Only orders with status "pending" or "confirmed" can be cancelled.
     *
     * @param orderId The UUID of the order to cancel
     * @return Result containing the cancelled order or error message
     */
    suspend fun cancelOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.cancelOrder(orderId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to cancel order")
        }
    }
}
