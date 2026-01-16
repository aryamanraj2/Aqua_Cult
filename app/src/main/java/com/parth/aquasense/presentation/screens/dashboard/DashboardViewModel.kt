package com.parth.aquasense.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.TankRepository
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Dashboard Screen
 * Manages loading dashboard summary with statistics and alerts
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tankRepository: TankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    /**
     * Load dashboard summary
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            when (val result = tankRepository.getDashboard()) {
                is Result.Success -> {
                    _uiState.value = DashboardUiState.Success(
                        dashboard = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = DashboardUiState.Error(
                        message = result.message ?: "Failed to load dashboard"
                    )
                }
            }
        }
    }

    /**
     * Refresh dashboard (for pull-to-refresh)
     */
    fun refresh() {
        val currentState = _uiState.value
        if (currentState !is DashboardUiState.Success) {
            loadDashboard()
            return
        }

        viewModelScope.launch {
            // Show refreshing indicator
            _uiState.value = currentState.copy(isRefreshing = true)

            when (val result = tankRepository.getDashboard()) {
                is Result.Success -> {
                    _uiState.value = DashboardUiState.Success(
                        dashboard = result.data,
                        isRefreshing = false
                    )
                }
                is Result.Error -> {
                    // On refresh error, keep old data but hide refreshing indicator
                    _uiState.value = currentState.copy(isRefreshing = false)
                }
            }
        }
    }

    /**
     * Retry loading after error
     */
    fun retry() {
        loadDashboard()
    }
}
