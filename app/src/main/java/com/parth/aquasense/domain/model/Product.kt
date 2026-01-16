package com.parth.aquasense.domain.model

/**
 * Domain model representing a product in the marketplace.
 *
 * Products can be of different categories (feed, medicine, equipment)
 * and are available for purchase by farmers.
 */
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val price: Double,
    val unit: String,
    val stockQuantity: Int,
    val imageUrl: String?,
    val manufacturer: String?,
    val createdAt: String,
    val updatedAt: String
)
