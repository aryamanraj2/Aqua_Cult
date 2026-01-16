package com.parth.aquasense.presentation.screens.tankform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parth.aquasense.data.repository.TankRepository
import com.parth.aquasense.domain.model.TankStatus
import com.parth.aquasense.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Tank Form Screen
 * Handles creating new tanks and editing existing tanks with validation
 */
@HiltViewModel
class TankFormViewModel @Inject constructor(
    private val tankRepository: TankRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tankId: String? = savedStateHandle["tankId"]

    private val _uiState = MutableStateFlow<TankFormUiState>(
        TankFormUiState.Ready(TankFormData())
    )
    val uiState: StateFlow<TankFormUiState> = _uiState.asStateFlow()

    init {
        // If tankId is provided, load existing tank for editing
        tankId?.let { loadTank(it) }
    }

    /**
     * Load existing tank for editing
     */
    private fun loadTank(tankId: String) {
        viewModelScope.launch {
            _uiState.value = TankFormUiState.LoadingTank

            when (val result = tankRepository.getTankById(tankId)) {
                is Result.Success -> {
                    val tank = result.data
                    _uiState.value = TankFormUiState.Ready(
                        formData = TankFormData(
                            tankId = tank.id,
                            name = tank.name,
                            capacity = tank.capacity.toString(),
                            currentStock = tank.currentStock.toString(),
                            species = tank.species,
                            location = tank.location ?: "",
                            status = tank.status
                        ),
                        isEditMode = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = TankFormUiState.Error(
                        message = result.message ?: "Failed to load tank"
                    )
                }
            }
        }
    }

    /**
     * Update form field value
     */
    fun updateField(field: TankFormField, value: String) {
        val currentState = _uiState.value
        if (currentState !is TankFormUiState.Ready) return

        val newFormData = when (field) {
            TankFormField.NAME -> currentState.formData.copy(name = value)
            TankFormField.CAPACITY -> currentState.formData.copy(capacity = value)
            TankFormField.CURRENT_STOCK -> currentState.formData.copy(currentStock = value)
            TankFormField.SPECIES_INPUT -> currentState.formData.copy(speciesInput = value)
            TankFormField.LOCATION -> currentState.formData.copy(location = value)
        }

        _uiState.value = currentState.copy(formData = newFormData)
    }

    /**
     * Update tank status
     */
    fun updateStatus(status: TankStatus) {
        val currentState = _uiState.value
        if (currentState !is TankFormUiState.Ready) return

        _uiState.value = currentState.copy(
            formData = currentState.formData.copy(status = status)
        )
    }

    /**
     * Add species to the list
     */
    fun addSpecies() {
        val currentState = _uiState.value
        if (currentState !is TankFormUiState.Ready) return

        val speciesInput = currentState.formData.speciesInput.trim()
        if (speciesInput.isEmpty()) return

        val updatedSpecies = currentState.formData.species + speciesInput
        _uiState.value = currentState.copy(
            formData = currentState.formData.copy(
                species = updatedSpecies,
                speciesInput = ""
            )
        )
    }

    /**
     * Remove species from the list
     */
    fun removeSpecies(species: String) {
        val currentState = _uiState.value
        if (currentState !is TankFormUiState.Ready) return

        val updatedSpecies = currentState.formData.species - species
        _uiState.value = currentState.copy(
            formData = currentState.formData.copy(species = updatedSpecies)
        )
    }

    /**
     * Validate form and save tank
     */
    fun saveTank() {
        val currentState = _uiState.value
        if (currentState !is TankFormUiState.Ready) return

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
            val result = if (currentState.isEditMode) {
                updateTank(formData)
            } else {
                createTank(formData)
            }

            when (result) {
                is Result.Success -> {
                    _uiState.value = TankFormUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = TankFormUiState.Ready(
                        formData = formData,
                        isEditMode = currentState.isEditMode,
                        isSaving = false
                    )
                    // Could show error in a different way, for now reset to ready state
                }
            }
        }
    }

    /**
     * Create new tank
     */
    private suspend fun createTank(formData: TankFormData) = tankRepository.createTank(
        name = formData.name.trim(),
        capacity = formData.capacity.toDouble(),
        currentStock = formData.currentStock.toInt(),
        species = formData.species,
        location = formData.location.trim().ifEmpty { null },
        status = formData.status.name.lowercase()
    )

    /**
     * Update existing tank
     */
    private suspend fun updateTank(formData: TankFormData) = tankRepository.updateTank(
        tankId = formData.tankId!!,
        name = formData.name.trim(),
        capacity = formData.capacity.toDouble(),
        currentStock = formData.currentStock.toInt(),
        species = formData.species,
        location = formData.location.trim().ifEmpty { null },
        status = formData.status.name.lowercase()
    )

    /**
     * Validate form fields
     */
    private fun validateForm(formData: TankFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate name
        if (formData.name.isBlank()) {
            errors["name"] = "Tank name is required"
        } else if (formData.name.length > 255) {
            errors["name"] = "Name must be 255 characters or less"
        }

        // Validate capacity
        val capacity = formData.capacity.toDoubleOrNull()
        if (capacity == null) {
            errors["capacity"] = "Capacity must be a valid number"
        } else if (capacity <= 0) {
            errors["capacity"] = "Capacity must be greater than 0"
        }

        // Validate current stock
        val currentStock = formData.currentStock.toIntOrNull()
        if (currentStock == null) {
            errors["currentStock"] = "Stock must be a valid number"
        } else if (currentStock < 0) {
            errors["currentStock"] = "Stock cannot be negative"
        }

        // Validate species
        if (formData.species.isEmpty()) {
            errors["species"] = "At least one species is required"
        }

        return errors
    }

    /**
     * Retry loading tank after error
     */
    fun retry() {
        tankId?.let { loadTank(it) }
    }
}

/**
 * Form field identifiers for updates
 */
enum class TankFormField {
    NAME,
    CAPACITY,
    CURRENT_STOCK,
    SPECIES_INPUT,
    LOCATION
}
