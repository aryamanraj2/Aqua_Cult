package com.parth.aquasense.data.remote.api

import com.parth.aquasense.data.remote.dto.DashboardDto
import com.parth.aquasense.data.remote.dto.TankCreateRequest
import com.parth.aquasense.data.remote.dto.TankDetailDto
import com.parth.aquasense.data.remote.dto.TankDto
import com.parth.aquasense.data.remote.dto.TankUpdateRequest
import com.parth.aquasense.data.remote.dto.WaterQualityCreateRequest
import com.parth.aquasense.data.remote.dto.WaterQualityDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Tank API interface - defines all tank-related endpoints
 *
 * Retrofit annotations explained:
 * - @GET: HTTP GET request
 * - @POST: HTTP POST request (creates resources)
 * - @PUT: HTTP PUT request (updates entire resource)
 * - @DELETE: HTTP DELETE request
 * - @Path: URL path parameter (e.g., /tanks/{id})
 * - @Query: URL query parameter (e.g., /tanks?skip=0&limit=10)
 * - @Body: Request body (JSON)
 * - @Header: HTTP header
 *
 * suspend: Makes function work with Kotlin Coroutines (async/await)
 */
interface TankApi {

    /**
     * Get dashboard summary
     * GET /tanks/dashboard
     */
    @GET("tanks/dashboard")
    suspend fun getDashboard(
        @Header("X-User-ID") userId: String = "default_user_001"
    ): DashboardDto

    /**
     * Get all tanks for current user
     * GET /tanks?skip=0&limit=100
     */
    @GET("tanks")
    suspend fun getTanks(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<TankDto>

    /**
     * Get specific tank by ID with water quality readings
     * GET /tanks/{tank_id}
     */
    @GET("tanks/{tank_id}")
    suspend fun getTankById(
        @Path("tank_id") tankId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): TankDetailDto

    /**
     * Create a new tank
     * POST /tanks
     */
    @POST("tanks")
    suspend fun createTank(
        @Body tank: TankCreateRequest,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): TankDto

    /**
     * Update existing tank
     * PUT /tanks/{tank_id}
     */
    @PUT("tanks/{tank_id}")
    suspend fun updateTank(
        @Path("tank_id") tankId: String,
        @Body tank: TankUpdateRequest,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): TankDto

    /**
     * Delete a tank
     * DELETE /tanks/{tank_id}
     * Returns 204 No Content on success
     */
    @DELETE("tanks/{tank_id}")
    suspend fun deleteTank(
        @Path("tank_id") tankId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    )

    /**
     * Add water quality reading to a tank
     * POST /tanks/{tank_id}/water-quality
     */
    @POST("tanks/{tank_id}/water-quality")
    suspend fun addWaterQualityReading(
        @Path("tank_id") tankId: String,
        @Body reading: WaterQualityCreateRequest,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): WaterQualityDto

    /**
     * Get water quality readings for a tank
     * GET /tanks/{tank_id}/water-quality?skip=0&limit=100
     */
    @GET("tanks/{tank_id}/water-quality")
    suspend fun getWaterQualityReadings(
        @Path("tank_id") tankId: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): List<WaterQualityDto>

    /**
     * Get latest water quality reading for a tank
     * GET /tanks/{tank_id}/water-quality/latest
     */
    @GET("tanks/{tank_id}/water-quality/latest")
    suspend fun getLatestWaterQuality(
        @Path("tank_id") tankId: String,
        @Header("X-User-ID") userId: String = "default_user_001"
    ): WaterQualityDto
}
