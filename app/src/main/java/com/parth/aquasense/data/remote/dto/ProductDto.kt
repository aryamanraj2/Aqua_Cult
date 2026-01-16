package com.parth.aquasense.data.remote.dto

import com.parth.aquasense.domain.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Product from the API.
 * Uses @SerialName to map snake_case API fields to camelCase Kotlin properties.
 */
@Serializable
data class ProductDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("unit") val unit: String,
    @SerialName("stock_quantity") val stockQuantity: Int,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("manufacturer") val manufacturer: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Extension function to convert ProductDto to domain Product model.
 */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category,
    description = description,
    price = price,
    unit = unit,
    stockQuantity = stockQuantity,
    imageUrl = imageUrl,
    manufacturer = manufacturer,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Extension function to convert list of ProductDto to list of domain Product models.
 */
fun List<ProductDto>.toDomain(): List<Product> = map { it.toDomain() }
