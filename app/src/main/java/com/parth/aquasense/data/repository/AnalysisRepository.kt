package com.parth.aquasense.data.repository

import com.parth.aquasense.data.mapper.toDomain
import com.parth.aquasense.data.remote.api.AnalysisApi
import com.parth.aquasense.data.remote.dto.DiseaseDetectionRequestDto
import com.parth.aquasense.domain.model.DiseaseDetection
import com.parth.aquasense.domain.model.TankAnalysis
import com.parth.aquasense.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analysis Repository - Single source of truth for AI analysis data
 *
 * Responsibilities:
 * - Communicate with backend API for disease detection and tank analysis
 * - Provide data to the UI layer via ViewModels
 *
 * Note: Local database caching can be added later for history feature
 */
@Singleton
class AnalysisRepository @Inject constructor(
    private val analysisApi: AnalysisApi
) {
    private val defaultUserId = "default_user_001"

    /**
     * Detect disease from fish image
     *
     * @param imageBase64 Base64 encoded image string
     * @param tankId Tank UUID (required for species context)
     * @param tankName Tank name for display
     * @param imageUri Local file path where image is saved
     * @param symptoms Optional list of observed symptoms
     *
     * @return Result<DiseaseDetection> with detection data or error
     */
    suspend fun detectDisease(
        imageBase64: String,
        tankId: String,
        tankName: String,
        imageUri: String?,
        symptoms: List<String>? = null
    ): Result<DiseaseDetection> = withContext(Dispatchers.IO) {
        try {
            // Create request DTO
            val request = DiseaseDetectionRequestDto(
                imageBase64 = imageBase64,
                tankId = tankId,
                symptoms = symptoms
            )

            // Call backend API
            val response = analysisApi.detectDisease(request, userId = defaultUserId)

            // Convert to domain model
            val detection = response.toDomain(tankId, tankName, imageUri)

            Result.Success(detection)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to detect disease. Please check your connection.")
        }
    }

    /**
     * Get AI-powered tank analysis with health score and recommendations
     *
     * @param tankId Tank UUID to analyze
     * @return Result<TankAnalysis> with AI-generated analysis or error
     */
    suspend fun getTankAnalysis(tankId: String): Result<TankAnalysis> = withContext(Dispatchers.IO) {
        try {
            // Call backend API
            val response = analysisApi.getTankAnalysis(tankId, userId = defaultUserId)

            // Convert to domain model
            val analysis = response.toDomain()

            Result.Success(analysis)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get tank analysis. Please check your connection.")
        }
    }
}
