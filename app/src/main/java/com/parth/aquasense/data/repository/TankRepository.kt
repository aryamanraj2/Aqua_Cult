package com.parth.aquasense.data.repository

import com.parth.aquasense.data.mapper.toDomain
import com.parth.aquasense.data.mapper.toDomainWaterQuality
import com.parth.aquasense.data.remote.api.TankApi
import com.parth.aquasense.data.remote.dto.TankCreateRequest
import com.parth.aquasense.data.remote.dto.TankUpdateRequest
import com.parth.aquasense.data.remote.dto.WaterQualityCreateRequest
import com.parth.aquasense.domain.model.Dashboard
import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.WaterQuality
import com.parth.aquasense.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tank Repository - Single source of truth for tank-related data
 *
 * Responsibilities:
 * - Fetch data from the API
 * - Convert DTOs to domain models
 * - Handle errors and wrap results in Result<T>
 * - Provide a clean, testable interface for the UI layer
 *
 * Why @Singleton?
 * - Only one instance across the app
 * - Can add in-memory caching later
 * - Reduces object creation overhead
 *
 * Why suspend functions?
 * - These are network calls that take time
 * - suspend allows them to be called from coroutines without blocking
 * - Automatically switches to IO dispatcher for network calls
 */
@Singleton
class TankRepository @Inject constructor(
    private val tankApi: TankApi
) {
    /**
     * Default user ID for this local-only app
     * In a production app, this would come from authentication
     */
    private val defaultUserId = "default_user_001"

    /**
     * Get dashboard summary with tank statistics and alerts
     *
     * Returns: Result<Dashboard>
     * - Success: Contains Dashboard with tank stats
     * - Error: Contains error message if API call fails
     */
    suspend fun getDashboard(): Result<Dashboard> = withContext(Dispatchers.IO) {
        try {
            // Fetch basic dashboard data from API
            val dashboardDto = tankApi.getDashboard(userId = defaultUserId)
            var dashboard = dashboardDto.toDomain()

            // Fetch all tanks to calculate extra stats that are missing from the backend dashboard endpoint
            val tanksResult = getTanks()
            if (tanksResult is Result.Success) {
                val tanks = tanksResult.data
                val activeTanks = tanks.filter { it.status == com.parth.aquasense.domain.model.TankStatus.ACTIVE }
                val activeTankCount = activeTanks.size
                
                // Calculate total volume of active tanks
                val totalVolume = activeTanks.sumOf { it.capacity }

                // Calculate average temperature of active tanks
                var tempSum = 0.0
                var tanksWithTemp = 0
                
                // We need to fetch latest water quality for each active tank to get temperature
                // In a real app, this should be a bulk API call or optimized
                activeTanks.forEach { tank ->
                    try {
                         val wqDto = tankApi.getLatestWaterQuality(tank.id, userId = defaultUserId)
                         if (wqDto.temperature != null) {
                             tempSum += wqDto.temperature
                             tanksWithTemp++
                         }
                    } catch (e: Exception) {
                        // Ignore errors for individual tank readings
                    }
                }
                
                val avgTemp = if (tanksWithTemp > 0) tempSum / tanksWithTemp else 0.0

                // Create updated dashboard object with calculated values
                dashboard = dashboard.copy(
                    activeTanks = activeTankCount,
                    totalVolume = totalVolume,
                    avgTemperature = avgTemp
                )
            } else {
                // If we fail to fetch tanks, just use defaults or 0 for new fields
                dashboard = dashboard.copy(
                    activeTanks = 0,
                    totalVolume = 0.0,
                    avgTemperature = 0.0
                )
            }
            
            Result.Success(dashboard)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch dashboard")
        }
    }

    /**
     * Get all tanks for the current user
     *
     * Returns: Result<List<Tank>>
     */
    suspend fun getTanks(): Result<List<Tank>> = withContext(Dispatchers.IO) {
        try {
            val response = tankApi.getTanks(userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch tanks")
        }
    }

    /**
     * Get specific tank by ID
     *
     * @param tankId The UUID of the tank to fetch
     * Returns: Result<Tank>
     */
    suspend fun getTankById(tankId: String): Result<Tank> = withContext(Dispatchers.IO) {
        try {
            val response = tankApi.getTankById(tankId, userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch tank details")
        }
    }

    /**
     * Create a new tank
     *
     * @param name Tank name
     * @param capacity Tank capacity in liters
     * @param currentStock Number of fish currently in tank
     * @param species List of fish species in the tank
     * @param location Optional location description
     * @param status Tank status (active/inactive/maintenance)
     *
     * Returns: Result<Tank> - The newly created tank
     */
    suspend fun createTank(
        name: String,
        capacity: Double,
        currentStock: Int,
        species: List<String>,
        location: String? = null,
        status: String = "active"
    ): Result<Tank> = withContext(Dispatchers.IO) {
        try {
            val request = TankCreateRequest(
                name = name,
                capacity = capacity,
                currentStock = currentStock,
                species = species,
                location = location,
                status = status
            )
            val response = tankApi.createTank(request, userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create tank")
        }
    }

    /**
     * Update existing tank
     *
     * @param tankId Tank UUID
     * All other parameters are optional - only send what needs to be updated
     *
     * Returns: Result<Tank> - The updated tank
     */
    suspend fun updateTank(
        tankId: String,
        name: String? = null,
        capacity: Double? = null,
        currentStock: Int? = null,
        species: List<String>? = null,
        location: String? = null,
        status: String? = null
    ): Result<Tank> = withContext(Dispatchers.IO) {
        try {
            val request = TankUpdateRequest(
                name = name,
                capacity = capacity,
                currentStock = currentStock,
                species = species,
                location = location,
                status = status
            )
            val response = tankApi.updateTank(tankId, request, userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update tank")
        }
    }

    /**
     * Delete a tank
     *
     * @param tankId Tank UUID to delete
     * Returns: Result<Unit> - Success with no data, or Error
     */
    suspend fun deleteTank(tankId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tankApi.deleteTank(tankId, userId = defaultUserId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete tank")
        }
    }

    /**
     * Add water quality reading to a tank
     *
     * @param tankId Tank UUID
     * @param ph pH level (6.5-8.5 is typical safe range)
     * @param temperature Temperature in Celsius
     * @param dissolvedOxygen Dissolved oxygen in mg/L
     * @param ammonia Ammonia level in mg/L
     * @param nitrite Nitrite level in mg/L
     * @param nitrate Nitrate level in mg/L
     * @param salinity Salinity in ppt (for saltwater tanks)
     * @param turbidity Turbidity in NTU
     *
     * Returns: Result<WaterQuality> - The created reading
     */
    suspend fun addWaterQualityReading(
        tankId: String,
        ph: Double? = null,
        temperature: Double? = null,
        dissolvedOxygen: Double? = null,
        ammonia: Double? = null,
        nitrite: Double? = null,
        nitrate: Double? = null,
        salinity: Double? = null,
        turbidity: Double? = null
    ): Result<WaterQuality> = withContext(Dispatchers.IO) {
        try {
            val request = WaterQualityCreateRequest(
                ph = ph,
                temperature = temperature,
                dissolvedOxygen = dissolvedOxygen,
                ammonia = ammonia,
                nitrite = nitrite,
                nitrate = nitrate,
                salinity = salinity,
                turbidity = turbidity
            )
            val response = tankApi.addWaterQualityReading(tankId, request, userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add water quality reading")
        }
    }

    /**
     * Get water quality readings for a tank
     *
     * @param tankId Tank UUID
     * @param limit Maximum number of readings to fetch
     *
     * Returns: Result<List<WaterQuality>> - List of readings, newest first
     */
    suspend fun getWaterQualityReadings(
        tankId: String,
        limit: Int = 100
    ): Result<List<WaterQuality>> = withContext(Dispatchers.IO) {
        try {
            val response = tankApi.getWaterQualityReadings(
                tankId,
                limit = limit,
                userId = defaultUserId
            )
            Result.Success(response.toDomainWaterQuality())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch water quality readings")
        }
    }

    /**
     * Get the latest water quality reading for a tank
     *
     * @param tankId Tank UUID
     * Returns: Result<WaterQuality> - The most recent reading
     */
    suspend fun getLatestWaterQuality(tankId: String): Result<WaterQuality> = withContext(Dispatchers.IO) {
        try {
            val response = tankApi.getLatestWaterQuality(tankId, userId = defaultUserId)
            Result.Success(response.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch latest water quality")
        }
    }
}
