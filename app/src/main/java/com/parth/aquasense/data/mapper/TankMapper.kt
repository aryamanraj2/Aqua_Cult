package com.parth.aquasense.data.mapper

import com.parth.aquasense.data.remote.dto.AlertDto
import com.parth.aquasense.data.remote.dto.DashboardDto
import com.parth.aquasense.data.remote.dto.TankAnalysisDto
import com.parth.aquasense.data.remote.dto.TankDetailDto
import com.parth.aquasense.data.remote.dto.TankDto
import com.parth.aquasense.data.remote.dto.WaterQualityDto
import com.parth.aquasense.domain.model.Alert
import com.parth.aquasense.domain.model.AlertType
import com.parth.aquasense.domain.model.Dashboard
import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.TankAnalysis
import com.parth.aquasense.domain.model.TankStatus
import com.parth.aquasense.domain.model.WaterQuality

/**
 * Mapper functions to convert DTOs (from API) to Domain Models (for UI)
 *
 * Why separate DTOs and Domain Models?
 * - DTOs: Represent the API response structure (snake_case, nullable, tied to backend)
 * - Domain Models: Represent what the UI needs (camelCase, non-null where possible, business logic)
 *
 * This separation:
 * - Protects the UI from API changes
 * - Allows adding UI-specific computed properties
 * - Makes testing easier
 */

/**
 * Convert TankDto (from API) to Tank (domain model)
 */
fun TankDto.toDomain(): Tank {
    return Tank(
        id = id,
        userId = userId,
        name = name,
        capacity = capacity,
        currentStock = currentStock,
        species = species,
        location = location,
        status = TankStatus.fromString(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convert WaterQualityDto to WaterQuality domain model
 */
fun WaterQualityDto.toDomain(): WaterQuality {
    return WaterQuality(
        id = id,
        tankId = tankId,
        ph = ph,
        temperature = temperature,
        dissolvedOxygen = dissolvedOxygen,
        ammonia = ammonia,
        nitrite = nitrite,
        nitrate = nitrate,
        salinity = salinity,
        turbidity = turbidity,
        createdAt = createdAt
    )
}

/**
 * Convert TankDetailDto (tank with water quality readings) to domain Tank
 *
 * Note: We return just the Tank, not the readings, since those can be
 * fetched separately if needed. This keeps the domain model simple.
 */
fun TankDetailDto.toDomain(): Tank {
    return Tank(
        id = id,
        userId = userId,
        name = name,
        capacity = capacity,
        currentStock = currentStock,
        species = species,
        location = location,
        status = TankStatus.fromString(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convert AlertDto to Alert domain model
 */
fun AlertDto.toDomain(): Alert {
    return Alert(
        tankId = tankId,
        tankName = tankName,
        type = AlertType.fromString(type),
        message = message
    )
}

/**
 * Convert DashboardDto to Dashboard domain model
 */
fun DashboardDto.toDomain(): Dashboard {
    return Dashboard(
        totalTanks = totalTanks,
        activeTanks = 0, // Calculated in Repository
        totalVolume = 0.0, // Calculated in Repository
        avgTemperature = 0.0, // Calculated in Repository
        totalFish = totalFish,
        tanksNeedingAttention = tanksNeedingAttention,
        recentAlerts = recentAlerts.map { it.toDomain() }
    )
}

/**
 * Extension function: Convert a list of TankDto to List<Tank>
 */
fun List<TankDto>.toDomain(): List<Tank> {
    return map { it.toDomain() }
}

/**
 * Extension function: Convert a list of WaterQualityDto to List<WaterQuality>
 */
fun List<WaterQualityDto>.toDomainWaterQuality(): List<WaterQuality> {
    return map { it.toDomain() }
}

/**
 * Convert TankAnalysisDto to TankAnalysis domain model
 *
 * Combines water quality analysis recommendations and general recommendations
 * into a single recommendations list, and alerts into warnings.
 */
fun TankAnalysisDto.toDomain(): TankAnalysis {
    // Combine all recommendations from different sources
    val allRecommendations = buildList {
        addAll(waterQualityAnalysis.recommendations)
        addAll(generalRecommendations)
    }

    // Create analysis text from water quality status
    val analysisText = "Water quality status: ${waterQualityAnalysis.status.replaceFirstChar { it.uppercase() }}"

    return TankAnalysis(
        tankId = tankId,
        tankName = tankName,
        healthScore = overallHealthScore.toInt(), // Convert to integer (0-100)
        analysis = analysisText,
        recommendations = allRecommendations,
        warnings = alerts,
        timestamp = timestamp
    )
}
