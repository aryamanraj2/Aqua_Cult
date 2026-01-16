package com.parth.aquasense.data.remote.api

import com.parth.aquasense.data.remote.dto.DiseaseDetectionRequestDto
import com.parth.aquasense.data.remote.dto.DiseaseDetectionResponseDto
import com.parth.aquasense.data.remote.dto.TankAnalysisDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Analysis API interface - Disease detection and AI analysis endpoints
 *
 * Backend base URL: /api/v1/analysis/
 */
interface AnalysisApi {

    /**
     * Detect fish diseases from image
     * POST /analysis/disease-detection
     *
     * @param request Contains image_base64, tank_id, and optional symptoms
     * @param userId User ID header (default for local development)
     * @return Disease detection results with ML predictions and AI recommendations
     */
    @POST("analysis/disease-detection")
    suspend fun detectDisease(
        @Body request: DiseaseDetectionRequestDto,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): DiseaseDetectionResponseDto

    /**
     * Get AI-powered tank analysis with health score and recommendations
     * GET /analysis/tank-analysis/{tankId}
     *
     * @param tankId Tank UUID to analyze
     * @param userId User ID header (default for local development)
     * @return AI-generated tank analysis with health score, recommendations, and warnings
     */
    @GET("analysis/tank-analysis/{tankId}")
    suspend fun getTankAnalysis(
        @Path("tankId") tankId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): TankAnalysisDto
}
