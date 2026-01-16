package com.parth.aquasense.presentation.screens.dashboard

import com.parth.aquasense.domain.model.Dashboard

/**
 * UI State for Dashboard Screen
 */
sealed class DashboardUiState {
    /**
     * Initial loading state
     */
    data object Loading : DashboardUiState()

    /**
     * Dashboard data loaded successfully
     * @param dashboard The dashboard summary data
     * @param isRefreshing Whether pull-to-refresh is active
     */
    data class Success(
        val dashboard: Dashboard,
        val isRefreshing: Boolean = false
    ) : DashboardUiState()

    /**
     * Error loading dashboard
     * @param message Error message to display
     */
    data class Error(val message: String) : DashboardUiState()
}
