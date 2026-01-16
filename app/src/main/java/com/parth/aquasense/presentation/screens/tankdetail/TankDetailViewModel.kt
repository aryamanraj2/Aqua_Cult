package com.parth.aquasense.presentation.screens.tankdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.AnalysisRepository
import com.parth.aquasense.data.repository.TankRepository
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Tank Detail Screen
 * Manages loading tank details, water quality, AI analysis, and tank operations (edit/delete)
 */
@HiltViewModel
class TankDetailViewModel @Inject constructor(
    private val tankRepository: TankRepository,
    private val analysisRepository: AnalysisRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tankId: String = checkNotNull(savedStateHandle["tankId"]) {
        "Tank ID is required"
    }

    private val _uiState = MutableStateFlow<TankDetailUiState>(TankDetailUiState.Loading)
    val uiState: StateFlow<TankDetailUiState> = _uiState.asStateFlow()

    init {
        loadTankDetails()
    }

    /**
     * Load tank details with latest water quality reading
     */
    fun loadTankDetails() {
        viewModelScope.launch {
            _uiState.value = TankDetailUiState.Loading

            when (val result = tankRepository.getTankById(tankId)) {
                is Result.Success -> {
                    // Load latest water quality reading
                    val waterQuality = when (val wqResult = tankRepository.getLatestWaterQuality(tankId)) {
                        is Result.Success -> wqResult.data
                        is Result.Error -> null // No water quality data yet
                    }

                    _uiState.value = TankDetailUiState.Success(
                        tank = result.data,
                        waterQuality = waterQuality
                    )
                }
                is Result.Error -> {
                    _uiState.value = TankDetailUiState.Error(
                        message = result.message ?: "Failed to load tank details"
                    )
                }
            }
        }
    }

    /**
     * Refresh tank details (for pull-to-refresh)
     */
    fun refresh() {
        val currentState = _uiState.value
        if (currentState !is TankDetailUiState.Success) {
            loadTankDetails()
            return
        }

        viewModelScope.launch {
            // Show refreshing indicator
            _uiState.value = currentState.copy(isRefreshing = true)

            when (val result = tankRepository.getTankById(tankId)) {
                is Result.Success -> {
                    // Load latest water quality reading
                    val waterQuality = when (val wqResult = tankRepository.getLatestWaterQuality(tankId)) {
                        is Result.Success -> wqResult.data
                        is Result.Error -> null
                    }

                    _uiState.value = TankDetailUiState.Success(
                        tank = result.data,
                        waterQuality = waterQuality,
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
     * Delete the current tank
     */
    fun deleteTank() {
        viewModelScope.launch {
            when (tankRepository.deleteTank(tankId)) {
                is Result.Success -> {
                    _uiState.value = TankDetailUiState.Deleted
                }
                is Result.Error -> {
                    val currentState = _uiState.value
                    if (currentState is TankDetailUiState.Success) {
                        // Show error but keep current state
                        // In a real app, you might show a Snackbar here
                        _uiState.value = TankDetailUiState.Error("Failed to delete tank")
                    }
                }
            }
        }
    }

    /**
     * Retry loading after error
     */
    fun retry() {
        loadTankDetails()
    }

    /**
     * Load AI-powered tank analysis
     * This fetches health score, recommendations, and warnings from the backend
     */
    fun loadTankAnalysis() {
        val currentState = _uiState.value
        if (currentState !is TankDetailUiState.Success) {
            return
        }

        viewModelScope.launch {
            // Show loading indicator
            _uiState.value = currentState.copy(
                isLoadingAnalysis = true,
                analysisError = null
            )

            when (val result = analysisRepository.getTankAnalysis(tankId)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        analysis = result.data,
                        isLoadingAnalysis = false,
                        analysisError = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        analysis = null,
                        isLoadingAnalysis = false,
                        analysisError = result.message ?: "Failed to load analysis"
                    )
                }
            }
        }
    }

    /**
     * Dismiss the analysis bottom sheet
     */
    fun dismissAnalysis() {
        val currentState = _uiState.value
        if (currentState !is TankDetailUiState.Success) {
            return
        }

        _uiState.value = currentState.copy(
            analysis = null,
            analysisError = null
        )
    }
}
