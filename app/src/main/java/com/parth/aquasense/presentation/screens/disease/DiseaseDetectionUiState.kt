package com.parth.aquasense.presentation.screens.disease

import android.net.Uri
import com.parth.aquasense.domain.model.DiseaseDetection
import com.parth.aquasense.domain.model.Tank

/**
 * UI State for Disease Detection Screen
 */
sealed class DiseaseDetectionUiState {
    /**
     * Initial state - ready to start detection
     */
    data object Initial : DiseaseDetectionUiState()

    /**
     * Analyzing image - show loading indicator
     */
    data object Analyzing : DiseaseDetectionUiState()

    /**
     * Analysis complete - disease detected
     */
    data class Success(val detection: DiseaseDetection) : DiseaseDetectionUiState()

    /**
     * Analysis failed - show error
     */
    data class Error(val message: String) : DiseaseDetectionUiState()
}

/**
 * View state for Disease Detection Screen
 * Contains all the UI-related state that doesn't fit in the sealed class
 */
data class DiseaseDetectionViewState(
    val selectedTank: Tank? = null,
    val capturedImageUri: Uri? = null,
    val isLoadingTanks: Boolean = false,
    val tanks: List<Tank> = emptyList(),
    val errorMessage: String? = null
)
