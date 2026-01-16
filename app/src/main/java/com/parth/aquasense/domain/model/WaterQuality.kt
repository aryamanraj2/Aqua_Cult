package com.parth.aquasense.domain.model

/**
 * Domain model for Water Quality readings
 *
 * Represents a snapshot of water quality parameters at a specific time
 */
data class WaterQuality(
    val id: String,
    val tankId: String,
    val ph: Double?,
    val temperature: Double?,
    val dissolvedOxygen: Double?,
    val ammonia: Double?,
    val nitrite: Double?,
    val nitrate: Double?,
    val salinity: Double?,
    val turbidity: Double?,
    val createdAt: String
) {
    /**
     * Check if water quality is within safe ranges
     * This is simplified - real ranges depend on fish species
     */
    val isSafe: Boolean
        get() {
            return (ph == null || ph in 6.5..8.5) &&
                   (temperature == null || temperature in 20.0..28.0) &&
                   (dissolvedOxygen == null || dissolvedOxygen >= 5.0) &&
                   (ammonia == null || ammonia < 0.5) &&
                   (nitrite == null || nitrite < 1.0) &&
                   (nitrate == null || nitrate < 40.0)
        }

    /**
     * Get a list of parameters that are out of range
     */
    val warnings: List<String>
        get() = buildList {
            ph?.let { if (it !in 6.5..8.5) add("pH is out of range") }
            temperature?.let { if (it !in 20.0..28.0) add("Temperature is abnormal") }
            dissolvedOxygen?.let { if (it < 5.0) add("Low dissolved oxygen") }
            ammonia?.let { if (it >= 0.5) add("High ammonia levels") }
            nitrite?.let { if (it >= 1.0) add("High nitrite levels") }
            nitrate?.let { if (it >= 40.0) add("High nitrate levels") }
        }
}
