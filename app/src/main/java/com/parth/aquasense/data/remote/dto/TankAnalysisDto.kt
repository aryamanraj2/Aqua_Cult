package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for Tank Analysis API
 *
 * Backend endpoint: GET /api/v1/analysis/tank-analysis/{tankId}
 *
 * Backend response structure:
 * {
 *   "tank_id": "uuid",
 *   "tank_name": "Tank Name",
 *   "overall_health_score": 85.0,
 *   "water_quality_analysis": {
 *     "status": "good",
 *     "recommendations": ["..."]
 *   },
 *   "general_recommendations": ["..."],
 *   "alerts": ["..."],
 *   "timestamp": "2025-12-26T10:30:00Z"
 * }
 */

/**
 * Water quality analysis sub-object
 */
@Serializable
data class WaterQualityAnalysisDto(
    @SerialName("status")
    val status: String, // e.g., "good", "warning", "critical"

    @SerialName("recommendations")
    val recommendations: List<String> = emptyList()
)

/**
 * Response from tank analysis endpoint
 */
@Serializable
data class TankAnalysisDto(
    @SerialName("tank_id")
    val tankId: String,

    @SerialName("tank_name")
    val tankName: String,

    @SerialName("overall_health_score")
    val overallHealthScore: Double, // 0.0 to 100.0

    @SerialName("water_quality_analysis")
    val waterQualityAnalysis: WaterQualityAnalysisDto,

    @SerialName("general_recommendations")
    val generalRecommendations: List<String> = emptyList(),

    @SerialName("alerts")
    val alerts: List<String> = emptyList(),

    @SerialName("timestamp")
    val timestamp: String // ISO 8601 format
)
