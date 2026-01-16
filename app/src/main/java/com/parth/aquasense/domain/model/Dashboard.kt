package com.parth.aquasense.domain.model

/**
 * Domain model for Dashboard summary
 *
 * Aggregates data from multiple tanks to show an overview
 */
data class Dashboard(
    val totalTanks: Int,
    val activeTanks: Int,
    val totalVolume: Double,
    val avgTemperature: Double,
    val totalFish: Int,
    val tanksNeedingAttention: Int,
    val recentAlerts: List<Alert>
) {
    /**
     * Check if there are any critical alerts
     */
    val hasCriticalAlerts: Boolean
        get() = recentAlerts.any { it.type == AlertType.CRITICAL }

    /**
     * Get percentage of tanks that are healthy
     */
    val healthPercentage: Int
        get() = if (totalTanks > 0) {
            ((totalTanks - tanksNeedingAttention) * 100) / totalTanks
        } else 100
}

/**
 * Alert for issues that need user attention
 */
data class Alert(
    val tankId: String,
    val tankName: String,
    val type: AlertType,
    val message: String
)

/**
 * Alert severity levels
 */
enum class AlertType(val displayName: String) {
    INFO("Info"),
    WARNING("Warning"),
    CRITICAL("Critical");

    companion object {
        fun fromString(value: String): AlertType {
            return when (value.lowercase()) {
                "info" -> INFO
                "warning" -> WARNING
                "critical" -> CRITICAL
                else -> INFO
            }
        }
    }
}
