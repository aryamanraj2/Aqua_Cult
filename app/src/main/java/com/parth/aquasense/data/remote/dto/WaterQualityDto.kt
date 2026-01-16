package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Water Quality reading response from API
 */
@Serializable
data class WaterQualityDto(
    @SerialName("id")
    val id: String,

    @SerialName("tank_id")
    val tankId: String,

    @SerialName("ph")
    val ph: Double? = null,

    @SerialName("temperature")
    val temperature: Double? = null,

    @SerialName("dissolved_oxygen")
    val dissolvedOxygen: Double? = null,

    @SerialName("ammonia")
    val ammonia: Double? = null,

    @SerialName("nitrite")
    val nitrite: Double? = null,

    @SerialName("nitrate")
    val nitrate: Double? = null,

    @SerialName("salinity")
    val salinity: Double? = null,

    @SerialName("turbidity")
    val turbidity: Double? = null,

    @SerialName("created_at")
    val createdAt: String
)

/**
 * Request for adding a water quality reading
 */
@Serializable
data class WaterQualityCreateRequest(
    @SerialName("ph")
    val ph: Double? = null,

    @SerialName("temperature")
    val temperature: Double? = null,

    @SerialName("dissolved_oxygen")
    val dissolvedOxygen: Double? = null,

    @SerialName("ammonia")
    val ammonia: Double? = null,

    @SerialName("nitrite")
    val nitrite: Double? = null,

    @SerialName("nitrate")
    val nitrate: Double? = null,

    @SerialName("salinity")
    val salinity: Double? = null,

    @SerialName("turbidity")
    val turbidity: Double? = null
)

/**
 * Tank with detailed water quality readings
 */
@Serializable
data class TankDetailDto(
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
    val updatedAt: String,

    @SerialName("water_quality_readings")
    val waterQualityReadings: List<WaterQualityDto> = emptyList()
)
