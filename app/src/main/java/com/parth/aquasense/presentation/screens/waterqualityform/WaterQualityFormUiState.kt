package com.parth.aquasense.presentation.screens.waterqualityform

import com.parth.aquasense.domain.model.WaterQuality

/**
 * UI state for Water Quality Form screen
 * Follows the pattern established in TankFormUiState
 */
sealed class WaterQualityFormUiState {
    /**
     * Loading state - fetching tank information
     */
    data object Loading : WaterQualityFormUiState()

    /**
     * Ready state - form is ready for user input
     * @param formData Current form data with validation errors
     * @param tankName Name of the tank (for display in header)
     * @param isSaving True when saving reading to backend
     */
    data class Ready(
        val formData: WaterQualityFormData,
        val tankName: String,
        val isSaving: Boolean = false
    ) : WaterQualityFormUiState()

    /**
     * Success state - reading saved successfully
     * @param waterQuality The saved water quality reading
     */
    data class Success(val waterQuality: WaterQuality) : WaterQualityFormUiState()

    /**
     * Error state - failed to load tank or save reading
     * @param message Error message to display
     */
    data class Error(val message: String) : WaterQualityFormUiState()
}
