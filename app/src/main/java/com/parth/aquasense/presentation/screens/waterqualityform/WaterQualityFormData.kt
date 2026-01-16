package com.parth.aquasense.presentation.screens.waterqualityform

/**
 * Form data class for water quality readings
 * All fields are String type for TextField binding
 */
data class WaterQualityFormData(
    // Required fields
    val ph: String = "",
    val temperature: String = "",
    val dissolvedOxygen: String = "",

    // Optional fields
    val ammonia: String = "",
    val nitrite: String = "",
    val nitrate: String = "",
    val salinity: String = "",
    val turbidity: String = "",

    // Field-level validation errors
    val errors: Map<String, String> = emptyMap()
) {
    /**
     * Form is valid if all required fields are present and no validation errors exist
     */
    val isValid: Boolean
        get() = ph.isNotBlank() &&
                temperature.isNotBlank() &&
                dissolvedOxygen.isNotBlank() &&
                !hasErrors

    /**
     * Check if form has any validation errors
     */
    val hasErrors: Boolean
        get() = errors.isNotEmpty()
}

/**
 * Enum for form field names to avoid string literals
 */
enum class WaterQualityFormField {
    PH,
    TEMPERATURE,
    DISSOLVED_OXYGEN,
    AMMONIA,
    NITRITE,
    NITRATE,
    SALINITY,
    TURBIDITY
}
