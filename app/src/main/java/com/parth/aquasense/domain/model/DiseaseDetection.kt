package com.parth.aquasense.domain.model

/**
 * Domain models for Disease Detection feature
 *
 * These models represent disease detection data in the app's domain layer,
 * independent of data source (API or local database)
 */

/**
 * Information about a specific disease detected
 */
data class DiseaseInfo(
    val name: String,
    val confidence: Double, // 0.0 to 1.0
    val description: String,
    val causes: List<String>,
    val symptoms: List<String>,
    val treatment: String,
    val prevention: List<String>
)

/**
 * Complete disease detection result
 */
data class DiseaseDetection(
    val id: String, // Local UUID for Room database
    val tankId: String,
    val tankName: String,
    val detectedDiseases: List<DiseaseInfo>, // All detected diseases (up to 3)
    val topDisease: DiseaseInfo?, // Highest confidence disease
    val recommendation: String,
    val severity: DiseaseSeverity,
    val urgentActionRequired: Boolean,
    val imageUri: String?, // Local file path to saved image
    val timestamp: Long // Unix timestamp in milliseconds
)

/**
 * Disease severity levels
 */
enum class DiseaseSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    companion object {
        fun fromString(value: String): DiseaseSeverity {
            return when (value.lowercase()) {
                "low" -> LOW
                "medium" -> MEDIUM
                "high" -> HIGH
                "critical" -> CRITICAL
                else -> MEDIUM // Default to medium if unknown
            }
        }
    }

    fun toDisplayString(): String {
        return when (this) {
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
            CRITICAL -> "Critical"
        }
    }
}
