package com.parth.aquasense.domain.model

/**
 * Domain model for AI-powered tank analysis
 *
 * This model represents the AI-generated recommendations and health analysis for a tank.
 * Received from the backend's /api/v1/analysis/tank-analysis/{tankId} endpoint.
 */
data class TankAnalysis(
    val tankId: String,
    val tankName: String,
    val healthScore: Int, // 0-100 health score
    val analysis: String, // AI-generated water quality analysis text
    val recommendations: List<String>, // List of AI recommendations
    val warnings: List<String>, // List of warnings/alerts
    val timestamp: String // ISO 8601 timestamp when analysis was generated
) {
    /**
     * Computed property: Get health status color category
     * - Excellent: > 85
     * - Good: 70-85
     * - Fair: 50-69
     * - Poor: < 50
     */
    val healthCategory: HealthCategory
        get() = when {
            healthScore >= 85 -> HealthCategory.EXCELLENT
            healthScore >= 70 -> HealthCategory.GOOD
            healthScore >= 50 -> HealthCategory.FAIR
            else -> HealthCategory.POOR
        }

    /**
     * Computed property: Check if any warnings exist
     */
    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()
}

/**
 * Health category enum for color-coding UI
 */
enum class HealthCategory(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor")
}
