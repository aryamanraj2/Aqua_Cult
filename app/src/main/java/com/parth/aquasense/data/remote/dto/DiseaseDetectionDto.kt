package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for Disease Detection API
 *
 * These DTOs match the backend schema from:
 * aquasense_backend/schemas/analysis.py
 */

/**
 * Disease information from ML model and AI analysis
 */
@Serializable
data class DiseaseInfoDto(
    @SerialName("name")
    val name: String,

    @SerialName("confidence")
    val confidence: Double, // 0.0 to 1.0

    @SerialName("description")
    val description: String,

    @SerialName("causes")
    val causes: List<String>,

    @SerialName("symptoms")
    val symptoms: List<String>,

    @SerialName("treatment")
    val treatment: String,

    @SerialName("prevention")
    val prevention: List<String>
)

/**
 * Response from disease detection endpoint
 */
@Serializable
data class DiseaseDetectionResponseDto(
    @SerialName("detected_diseases")
    val detectedDiseases: List<DiseaseInfoDto>,

    @SerialName("recommendation")
    val recommendation: String,

    @SerialName("severity")
    val severity: String, // low, medium, high, critical

    @SerialName("urgent_action_required")
    val urgentActionRequired: Boolean,

    @SerialName("timestamp")
    val timestamp: String // ISO 8601 format
)

/**
 * Request body for disease detection
 */
@Serializable
data class DiseaseDetectionRequestDto(
    @SerialName("image_base64")
    val imageBase64: String,

    @SerialName("tank_id")
    val tankId: String,

    @SerialName("symptoms")
    val symptoms: List<String>? = null
)
