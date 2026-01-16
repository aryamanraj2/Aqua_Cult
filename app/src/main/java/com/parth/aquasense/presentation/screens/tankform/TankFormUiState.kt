package com.parth.aquasense.presentation.screens.tankform

import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.TankStatus

/**
 * UI State for Tank Form Screen
 */
sealed class TankFormUiState {
    /**
     * Loading existing tank data (for edit mode)
     */
    data object LoadingTank : TankFormUiState()

    /**
     * Form is ready for input
     * @param formData Current form field values
     * @param isEditMode Whether editing existing tank (true) or creating new (false)
     * @param isSaving Whether save operation is in progress
     */
    data class Ready(
        val formData: TankFormData,
        val isEditMode: Boolean = false,
        val isSaving: Boolean = false
    ) : TankFormUiState()

    /**
     * Tank saved successfully
     * @param tank The saved tank
     */
    data class Success(val tank: Tank) : TankFormUiState()

    /**
     * Error loading tank or saving
     * @param message Error message to display
     */
    data class Error(val message: String) : TankFormUiState()
}

/**
 * Form data model for tank form fields
 */
data class TankFormData(
    val tankId: String? = null,
    val name: String = "",
    val capacity: String = "",
    val currentStock: String = "0",
    val species: List<String> = emptyList(),
    val speciesInput: String = "",
    val location: String = "",
    val status: TankStatus = TankStatus.ACTIVE,
    val errors: Map<String, String> = emptyMap()
) {
    /**
     * Check if form has any validation errors
     */
    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    /**
     * Check if all required fields are filled (basic check)
     */
    val isValid: Boolean
        get() = name.isNotBlank() &&
                capacity.isNotBlank() &&
                currentStock.isNotBlank() &&
                species.isNotEmpty() &&
                !hasErrors
}
