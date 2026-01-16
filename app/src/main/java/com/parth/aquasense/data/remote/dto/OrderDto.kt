package com.parth.aquasense.data.remote.dto

import com.parth.aquasense.domain.model.Order
import com.parth.aquasense.domain.model.OrderItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Order from the API.
 */
@Serializable
data class OrderDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("status") val status: String,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("notes") val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Data Transfer Object for OrderItem from the API.
 */
@Serializable
data class OrderItemDto(
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") val price: Double
)

/**
 * Request DTO for creating a new order.
 */
@Serializable
data class OrderCreateRequest(
    @SerialName("items") val items: List<OrderItemCreateDto>,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("notes") val notes: String? = null
)

/**
 * Request DTO for order item when creating an order.
 */
@Serializable
data class OrderItemCreateDto(
    @SerialName("product_id") val productId: String,
    @SerialName("quantity") val quantity: Int
)

/**
 * Extension function to convert OrderDto to domain Order model.
 */
fun OrderDto.toDomain(): Order = Order(
    id = id,
    userId = userId,
    items = items.map { it.toDomain() },
    totalAmount = totalAmount,
    status = status,
    deliveryAddress = deliveryAddress,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Extension function to convert OrderItemDto to domain OrderItem model.
 */
fun OrderItemDto.toDomain(): OrderItem = OrderItem(
    productId = productId,
    productName = null,  // Backend doesn't return this, fetched separately
    quantity = quantity,
    price = price
)

/**
 * Extension function to convert list of OrderDto to list of domain Order models.
 */
fun List<OrderDto>.toDomain(): List<Order> = map { it.toDomain() }
