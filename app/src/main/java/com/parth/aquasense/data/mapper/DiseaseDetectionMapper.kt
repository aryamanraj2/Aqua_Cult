package com.parth.aquasense.data.mapper

import com.parth.aquasense.data.remote.dto.DiseaseDetectionResponseDto
import com.parth.aquasense.data.remote.dto.DiseaseInfoDto
import com.parth.aquasense.domain.model.DiseaseDetection
import com.parth.aquasense.domain.model.DiseaseInfo
import com.parth.aquasense.domain.model.DiseaseSeverity
import java.util.UUID

/**
 * Mappers for converting Disease Detection DTOs to domain models
 */

/**
 * Convert DiseaseInfoDto to DiseaseInfo domain model
 */
fun DiseaseInfoDto.toDomain(): DiseaseInfo {
    return DiseaseInfo(
        name = name,
        confidence = confidence,
        description = description,
        causes = causes,
        symptoms = symptoms,
        treatment = treatment,
        prevention = prevention
    )
}

/**
 * Convert list of DiseaseInfoDto to domain models
 */
fun List<DiseaseInfoDto>.toDomainDiseaseInfo(): List<DiseaseInfo> {
    return map { it.toDomain() }
}

/**
 * Convert DiseaseDetectionResponseDto to DiseaseDetection domain model
 *
 * @param tankId Tank ID for the detection
 * @param tankName Tank name for display
 * @param imageUri Local file path where image is saved
 */
fun DiseaseDetectionResponseDto.toDomain(
    tankId: String,
    tankName: String,
    imageUri: String?
): DiseaseDetection {
    val diseases = detectedDiseases.toDomainDiseaseInfo()

    return DiseaseDetection(
        id = UUID.randomUUID().toString(),
        tankId = tankId,
        tankName = tankName,
        detectedDiseases = diseases,
        topDisease = diseases.maxByOrNull { it.confidence },
        recommendation = recommendation,
        severity = DiseaseSeverity.fromString(severity),
        urgentActionRequired = urgentActionRequired,
        imageUri = imageUri,
        timestamp = System.currentTimeMillis()
    )
}
