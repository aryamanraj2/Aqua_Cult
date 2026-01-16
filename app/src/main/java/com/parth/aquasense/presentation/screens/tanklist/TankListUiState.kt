package com.parth.aquasense.presentation.screens.tanklist

import com.parth.aquasense.domain.model.Tank

/**
 * UI State for Tank List Screen
 *
 * Why sealed class?
 * - Exhaustive when expressions (compiler forces handling all cases)
 * - Type-safe state management
 * - Clear separation of Loading/Success/Error states
 */
sealed class TankListUiState {
    /**
     * Initial/Loading state - Showing progress indicator
     */
    data object Loading : TankListUiState()

    /**
     * Success state - Tanks loaded successfully
     * @param tanks List of tanks to display
     */
    data class Success(
        val tanks: List<Tank>
    ) : TankListUiState()

    /**
     * Error state - Failed to load tanks
     * @param message Error message to display
     */
    data class Error(
        val message: String
    ) : TankListUiState()
}
