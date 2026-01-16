package com.parth.aquasense.domain.model

/**
 * Domain model for Tank - represents a fish tank in the app
 *
 * This is different from TankDto (which comes from the API).
 * Domain models:
 * - Are optimized for the UI layer
 * - Use Kotlin-friendly types (no nullability from JSON)
 * - Can have computed properties
 * - Are independent of API changes
 */
data class Tank(
    val id: String,
    val userId: String,
    val name: String,
    val capacity: Double,
    val currentStock: Int,
    val species: List<String>,
    val location: String?,
    val status: TankStatus,
    val createdAt: String,
    val updatedAt: String
) {
    /**
     * Computed property: Calculate how full the tank is
     * This business logic lives in the domain model, not the DTO
     */
    val stockPercentage: Int
        get() = if (capacity > 0) {
            ((currentStock / capacity) * 100).toInt()
        } else 0

    /**
     * Computed property: Check if tank is overstocked
     */
    val isOverstocked: Boolean
        get() = stockPercentage > 100

    /**
     * Computed property: Get a display-friendly species list
     */
    val speciesDisplay: String
        get() = species.joinToString(", ")
}

/**
 * Tank status enum with display-friendly values
 */
enum class TankStatus(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    MAINTENANCE("Maintenance");

    companion object {
        /**
         * Convert API string to enum
         */
        fun fromString(value: String): TankStatus {
            return when (value.lowercase()) {
                "active" -> ACTIVE
                "inactive" -> INACTIVE
                "maintenance" -> MAINTENANCE
                else -> ACTIVE // Default to active if unknown
            }
        }
    }
}
