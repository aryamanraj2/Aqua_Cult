package com.parth.aquasense.presentation.screens.tankdetail

import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.model.TankAnalysis
import com.parth.aquasense.domain.model.WaterQuality

/**
 * UI State for Tank Detail Screen
 */
sealed class TankDetailUiState {
    /**
     * Initial loading state
     */
    data object Loading : TankDetailUiState()

    /**
     * Tank details loaded successfully
     * @param tank The tank details
     * @param waterQuality Latest water quality reading (null if no readings exist)
     * @param isRefreshing Whether pull-to-refresh is active
     * @param analysis AI-generated tank analysis (null if not loaded yet)
     * @param isLoadingAnalysis Whether analysis is currently being loaded
     * @param analysisError Error message if analysis loading failed
     */
    data class Success(
        val tank: Tank,
        val waterQuality: WaterQuality?,
        val isRefreshing: Boolean = false,
        val analysis: TankAnalysis? = null,
        val isLoadingAnalysis: Boolean = false,
        val analysisError: String? = null
    ) : TankDetailUiState()

    /**
     * Error loading tank details
     * @param message Error message to display
     */
    data class Error(val message: String) : TankDetailUiState()

    /**
     * Tank deleted successfully - navigate back
     */
    data object Deleted : TankDetailUiState()
}
