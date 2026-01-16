package com.parth.aquasense.data.remote.api

import com.parth.aquasense.data.remote.dto.OrderCreateRequest
import com.parth.aquasense.data.remote.dto.OrderDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Order-related endpoints.
 *
 * All endpoints use the default user ID header for authentication.
 */
interface OrderApi {

    /**
     * Create a new order.
     *
     * @param request Order creation request with items and delivery details
     * @return Created order with calculated total and status
     */
    @POST("orders")
    suspend fun createOrder(
        @Body request: OrderCreateRequest,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): OrderDto

    /**
     * Get all orders for the current user.
     *
     * @param skip Number of orders to skip (pagination)
     * @param limit Maximum number of orders to return
     * @return List of orders sorted by creation date (newest first)
     */
    @GET("orders")
    suspend fun getOrders(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<OrderDto>

    /**
     * Get a specific order by ID.
     *
     * @param orderId The UUID of the order
     * @return Order details including items and status
     */
    @GET("orders/{order_id}")
    suspend fun getOrderById(
        @Path("order_id") orderId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): OrderDto

    /**
     * Cancel an order.
     * Only orders with status "pending" or "confirmed" can be cancelled.
     *
     * @param orderId The UUID of the order to cancel
     * @return Updated order with "cancelled" status
     */
    @POST("orders/{order_id}/cancel")
    suspend fun cancelOrder(
        @Path("order_id") orderId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): OrderDto
}
