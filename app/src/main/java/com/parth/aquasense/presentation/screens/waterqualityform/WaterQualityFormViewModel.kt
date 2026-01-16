package com.parth.aquasense.presentation.screens.waterqualityform

import androidx.lifecycle.SavedStateHandle
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
 * ViewModel for Water Quality Form Screen
 * Handles creating new water quality readings with validation
 */
@HiltViewModel
class WaterQualityFormViewModel @Inject constructor(
    private val tankRepository: TankRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tankId: String = savedStateHandle["tankId"]
        ?: throw IllegalArgumentException("tankId is required for WaterQualityFormScreen")

    private val _uiState = MutableStateFlow<WaterQualityFormUiState>(
        WaterQualityFormUiState.Loading
    )
    val uiState: StateFlow<WaterQualityFormUiState> = _uiState.asStateFlow()

    init {
        loadTankInfo()
    }

    /**
     * Load tank information to display tank name in header
     */
    private fun loadTankInfo() {
        viewModelScope.launch {
            when (val result = tankRepository.getTankById(tankId)) {
                is Result.Success -> {
                    _uiState.value = WaterQualityFormUiState.Ready(
                        formData = WaterQualityFormData(),
                        tankName = result.data.name
                    )
                }
                is Result.Error -> {
                    _uiState.value = WaterQualityFormUiState.Error(
                        message = result.message ?: "Failed to load tank information"
                    )
                }
            }
        }
    }

    /**
     * Update form field value
     */
    fun updateField(field: WaterQualityFormField, value: String) {
        val currentState = _uiState.value
        if (currentState !is WaterQualityFormUiState.Ready) return

        val newFormData = when (field) {
            WaterQualityFormField.PH -> currentState.formData.copy(ph = value)
            WaterQualityFormField.TEMPERATURE -> currentState.formData.copy(temperature = value)
            WaterQualityFormField.DISSOLVED_OXYGEN -> currentState.formData.copy(dissolvedOxygen = value)
            WaterQualityFormField.AMMONIA -> currentState.formData.copy(ammonia = value)
            WaterQualityFormField.NITRITE -> currentState.formData.copy(nitrite = value)
            WaterQualityFormField.NITRATE -> currentState.formData.copy(nitrate = value)
            WaterQualityFormField.SALINITY -> currentState.formData.copy(salinity = value)
            WaterQualityFormField.TURBIDITY -> currentState.formData.copy(turbidity = value)
        }

        // Clear validation errors for the updated field
        val updatedErrors = currentState.formData.errors.toMutableMap()
        updatedErrors.remove(field.name.lowercase())

        _uiState.value = currentState.copy(
            formData = newFormData.copy(errors = updatedErrors)
        )
    }

    /**
     * Validate form and save water quality reading
     */
    fun saveReading() {
        val currentState = _uiState.value
        if (currentState !is WaterQualityFormUiState.Ready) return

        val formData = currentState.formData
        val errors = validateForm(formData)

        if (errors.isNotEmpty()) {
            _uiState.value = currentState.copy(
                formData = formData.copy(errors = errors)
            )
            return
        }

        // Clear errors and start saving
        _uiState.value = currentState.copy(
            formData = formData.copy(errors = emptyMap()),
            isSaving = true
        )

        viewModelScope.launch {
            val result = tankRepository.addWaterQualityReading(
                tankId = tankId,
                ph = formData.ph.toDouble(),
                temperature = formData.temperature.toDouble(),
                dissolvedOxygen = formData.dissolvedOxygen.toDouble(),
                ammonia = formData.ammonia.toDoubleOrNull(),
                nitrite = formData.nitrite.toDoubleOrNull(),
                nitrate = formData.nitrate.toDoubleOrNull(),
                salinity = formData.salinity.toDoubleOrNull(),
                turbidity = formData.turbidity.toDoubleOrNull()
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = WaterQualityFormUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = WaterQualityFormUiState.Ready(
                        formData = formData,
                        tankName = currentState.tankName,
                        isSaving = false
                    )
                    // Error message could be shown in UI, for now reset to ready state
                }
            }
        }
    }

    /**
     * Validate form fields according to backend constraints
     */
    private fun validateForm(formData: WaterQualityFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate required fields
        if (formData.ph.isBlank()) {
            errors["ph"] = "pH is required"
        } else {
            val ph = formData.ph.toDoubleOrNull()
            if (ph == null) {
                errors["ph"] = "pH must be a valid number"
            } else if (ph < 0 || ph > 14) {
                errors["ph"] = "pH must be between 0 and 14"
            }
        }

        if (formData.temperature.isBlank()) {
            errors["temperature"] = "Temperature is required"
        } else {
            val temp = formData.temperature.toDoubleOrNull()
            if (temp == null) {
                errors["temperature"] = "Temperature must be a valid number"
            } else if (temp < -10 || temp > 50) {
                errors["temperature"] = "Temperature must be between -10°C and 50°C"
            }
        }

        if (formData.dissolvedOxygen.isBlank()) {
            errors["dissolved_oxygen"] = "Dissolved oxygen is required"
        } else {
            val dissolvedOxygen = formData.dissolvedOxygen.toDoubleOrNull()
            if (dissolvedOxygen == null) {
                errors["dissolved_oxygen"] = "Dissolved oxygen must be a valid number"
            } else if (dissolvedOxygen < 0) {
                errors["dissolved_oxygen"] = "Dissolved oxygen cannot be negative"
            }
        }

        // Validate optional fields (only if provided)
        if (formData.ammonia.isNotBlank()) {
            val ammonia = formData.ammonia.toDoubleOrNull()
            if (ammonia == null) {
                errors["ammonia"] = "Ammonia must be a valid number"
            } else if (ammonia < 0) {
                errors["ammonia"] = "Ammonia cannot be negative"
            }
        }

        if (formData.nitrite.isNotBlank()) {
            val nitrite = formData.nitrite.toDoubleOrNull()
            if (nitrite == null) {
                errors["nitrite"] = "Nitrite must be a valid number"
            } else if (nitrite < 0) {
                errors["nitrite"] = "Nitrite cannot be negative"
            }
        }

        if (formData.nitrate.isNotBlank()) {
            val nitrate = formData.nitrate.toDoubleOrNull()
            if (nitrate == null) {
                errors["nitrate"] = "Nitrate must be a valid number"
            } else if (nitrate < 0) {
                errors["nitrate"] = "Nitrate cannot be negative"
            }
        }

        if (formData.salinity.isNotBlank()) {
            val salinity = formData.salinity.toDoubleOrNull()
            if (salinity == null) {
                errors["salinity"] = "Salinity must be a valid number"
            } else if (salinity < 0) {
                errors["salinity"] = "Salinity cannot be negative"
            }
        }

        if (formData.turbidity.isNotBlank()) {
            val turbidity = formData.turbidity.toDoubleOrNull()
            if (turbidity == null) {
                errors["turbidity"] = "Turbidity must be a valid number"
            } else if (turbidity < 0) {
                errors["turbidity"] = "Turbidity cannot be negative"
            }
        }

        return errors
    }

    /**
     * Retry loading tank info after error
     */
    fun retry() {
        _uiState.value = WaterQualityFormUiState.Loading
        loadTankInfo()
    }
}
