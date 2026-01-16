package com.parth.aquasense.presentation.screens.tanklist

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
 * ViewModel for Tank List Screen
 *
 * Responsibilities:
 * - Load tanks from repository
 * - Manage UI state (Loading/Success/Error)
 * - Handle refresh functionality
 * - Provide data to UI layer
 *
 * @HiltViewModel: Enables Hilt to inject dependencies
 * @Inject: Constructor injection - Hilt provides TankRepository
 */
@HiltViewModel
class TankListViewModel @Inject constructor(
    private val tankRepository: TankRepository
) : ViewModel() {

    /**
     * Private mutable state - only ViewModel can modify
     */
    private val _uiState = MutableStateFlow<TankListUiState>(TankListUiState.Loading)

    /**
     * Public immutable state - UI observes this
     * asStateFlow() prevents external modification
     */
    val uiState: StateFlow<TankListUiState> = _uiState.asStateFlow()

    /**
     * Load tanks when ViewModel is created
     */
    init {
        loadTanks()
    }

    /**
     * Load tanks from repository
     *
     * viewModelScope.launch:
     * - Coroutine tied to ViewModel lifecycle
     * - Automatically cancelled when ViewModel is destroyed
     * - Prevents memory leaks
     */
    fun loadTanks() {
        viewModelScope.launch {
            // Set loading state
            _uiState.value = TankListUiState.Loading

            // Fetch tanks from repository
            when (val result = tankRepository.getTanks()) {
                is Result.Success -> {
                    _uiState.value = TankListUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = TankListUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Refresh tanks (for pull-to-refresh)
     * Same as loadTanks but called by user action
     */
    fun refresh() {
        loadTanks()
    }
}
