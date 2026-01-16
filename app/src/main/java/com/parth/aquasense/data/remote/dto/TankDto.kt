package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Tank responses from the API.
 *
 * @Serializable: Tells Kotlin Serialization this class can be converted to/from JSON
 * @SerialName: Maps Kotlin property names to JSON field names (snake_case in backend)
 */

@Serializable
data class TankDto(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("name")
    val name: String,

    @SerialName("capacity")
    val capacity: Double,

    @SerialName("current_stock")
    val currentStock: Int,

    @SerialName("species")
    val species: List<String>,

    @SerialName("location")
    val location: String? = null,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

/**
 * Request body for creating a tank
 */
@Serializable
data class TankCreateRequest(
    @SerialName("name")
    val name: String,

    @SerialName("capacity")
    val capacity: Double,

    @SerialName("current_stock")
    val currentStock: Int,

    @SerialName("species")
    val species: List<String>,

    @SerialName("location")
    val location: String? = null,

    @SerialName("status")
    val status: String = "active"
)

/**
 * Request body for updating a tank
 */
@Serializable
data class TankUpdateRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("capacity")
    val capacity: Double? = null,

    @SerialName("current_stock")
    val currentStock: Int? = null,

    @SerialName("species")
    val species: List<String>? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("status")
    val status: String? = null
)
