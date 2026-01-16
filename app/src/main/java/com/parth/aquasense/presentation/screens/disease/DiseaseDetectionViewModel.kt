package com.parth.aquasense.presentation.screens.disease

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.AnalysisRepository
import com.parth.aquasense.data.repository.TankRepository
import com.parth.aquasense.domain.model.Tank
import com.parth.aquasense.domain.util.Result
import com.parth.aquasense.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Disease Detection Screen
 *
 * Responsibilities:
 * - Load list of tanks for selection
 * - Handle image capture/selection
 * - Process image and send to backend for analysis
 * - Manage UI state for loading, success, error states
 */
@HiltViewModel
class DiseaseDetectionViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository,
    private val tankRepository: TankRepository
) : ViewModel() {

    // Main UI state for disease detection flow
    private val _uiState = MutableStateFlow<DiseaseDetectionUiState>(DiseaseDetectionUiState.Initial)
    val uiState: StateFlow<DiseaseDetectionUiState> = _uiState.asStateFlow()

    // Additional view state (tanks, selected tank, captured image)
    private val _viewState = MutableStateFlow(DiseaseDetectionViewState())
    val viewState: StateFlow<DiseaseDetectionViewState> = _viewState.asStateFlow()

    init {
        loadTanks()
    }

    /**
     * Load all tanks for selection dropdown
     */
    private fun loadTanks() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoadingTanks = true)

            when (val result = tankRepository.getTanks()) {
                is Result.Success -> {
                    _viewState.value = _viewState.value.copy(
                        tanks = result.data,
                        isLoadingTanks = false,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    _viewState.value = _viewState.value.copy(
                        tanks = emptyList(),
                        isLoadingTanks = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * User selected a tank from dropdown
     */
    fun selectTank(tank: Tank) {
        _viewState.value = _viewState.value.copy(selectedTank = tank)
    }

    /**
     * User captured or selected an image
     */
    fun onImageCaptured(imageUri: Uri) {
        _viewState.value = _viewState.value.copy(capturedImageUri = imageUri)
    }

    /**
     * User tapped "Analyze" button
     * Process image and send to backend
     */
    fun analyzeImage(context: Context) {
        val tank = _viewState.value.selectedTank
        val imageUri = _viewState.value.capturedImageUri

        // Validation
        if (tank == null) {
            _uiState.value = DiseaseDetectionUiState.Error("Please select a tank first")
            return
        }

        if (imageUri == null) {
            _uiState.value = DiseaseDetectionUiState.Error("Please capture or select an image")
            return
        }

        viewModelScope.launch {
            _uiState.value = DiseaseDetectionUiState.Analyzing

            try {
                // Compress image to reduce upload size
                val compressedUri = ImageUtils.compressImage(imageUri, context) ?: imageUri

                // Convert to base64 for API
                val base64 = ImageUtils.imageToBase64(compressedUri, context)
                if (base64 == null) {
                    _uiState.value = DiseaseDetectionUiState.Error(
                        "Failed to process image. Please try again."
                    )
                    return@launch
                }

                // Optional: Save image to internal storage for history
                val savedImagePath = ImageUtils.saveImageToInternalStorage(compressedUri, context)

                // Call backend API
                when (val result = analysisRepository.detectDisease(
                    imageBase64 = base64,
                    tankId = tank.id,
                    tankName = tank.name,
                    imageUri = savedImagePath
                )) {
                    is Result.Success -> {
                        _uiState.value = DiseaseDetectionUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = DiseaseDetectionUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DiseaseDetectionUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Reset state to initial (for retrying after error or starting new detection)
     */
    fun resetState() {
        _uiState.value = DiseaseDetectionUiState.Initial
        _viewState.value = _viewState.value.copy(
            capturedImageUri = null
        )
    }

    /**
     * Retry loading tanks if failed
     */
    fun retryLoadTanks() {
        loadTanks()
    }

    /**
     * Clear selected image (user wants to retake photo)
     */
    fun clearImage() {
        _viewState.value = _viewState.value.copy(capturedImageUri = null)
        _uiState.value = DiseaseDetectionUiState.Initial
    }
}
