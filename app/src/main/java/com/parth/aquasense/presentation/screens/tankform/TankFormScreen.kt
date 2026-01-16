package com.parth.aquasense.presentation.screens.tankform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.TankStatus
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Tank Form Screen
 * Allows creating new tanks and editing existing tanks with validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: TankFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back after successful save
    LaunchedEffect(uiState) {
        if (uiState is TankFormUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TankFormTopBar(
                title = when (uiState) {
                    is TankFormUiState.Ready -> {
                        if ((uiState as TankFormUiState.Ready).isEditMode) "Edit Tank" else "Add Tank"
                    }
                    else -> "Tank Form"
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TankFormUiState.LoadingTank -> {
                LoadingContent(paddingValues)
            }

            is TankFormUiState.Ready -> {
                FormContent(
                    formData = state.formData,
                    isSaving = state.isSaving,
                    onFieldChange = viewModel::updateField,
                    onStatusChange = viewModel::updateStatus,
                    onAddSpecies = viewModel::addSpecies,
                    onRemoveSpecies = viewModel::removeSpecies,
                    onSave = viewModel::saveTank,
                    paddingValues = paddingValues
                )
            }

            is TankFormUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    paddingValues = paddingValues
                )
            }

            is TankFormUiState.Success -> {
                // Navigation handled by LaunchedEffect
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TankFormTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FormContent(
    formData: TankFormData,
    isSaving: Boolean,
    onFieldChange: (TankFormField, String) -> Unit,
    onStatusChange: (TankStatus) -> Unit,
    onAddSpecies: () -> Unit,
    onRemoveSpecies: (String) -> Unit,
    onSave: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tank Name
        OutlinedTextField(
            value = formData.name,
            onValueChange = { onFieldChange(TankFormField.NAME, it) },
            label = { Text("Tank Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("name"),
            supportingText = formData.errors["name"]?.let { { Text(it) } },
            singleLine = true,
            enabled = !isSaving
        )

        // Capacity
        OutlinedTextField(
            value = formData.capacity,
            onValueChange = { onFieldChange(TankFormField.CAPACITY, it) },
            label = { Text("Capacity (liters) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("capacity"),
            supportingText = formData.errors["capacity"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Current Stock
        OutlinedTextField(
            value = formData.currentStock,
            onValueChange = { onFieldChange(TankFormField.CURRENT_STOCK, it) },
            label = { Text("Current Stock (fish count) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("currentStock"),
            supportingText = formData.errors["currentStock"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = !isSaving
        )

        // Species Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Species *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Species input with Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = formData.speciesInput,
                    onValueChange = { onFieldChange(TankFormField.SPECIES_INPUT, it) },
                    label = { Text("Add species") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSaving
                )

                FilledTonalIconButton(
                    onClick = onAddSpecies,
                    enabled = formData.speciesInput.isNotBlank() && !isSaving,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add species"
                    )
                }
            }

            // Species chips
            if (formData.species.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    formData.species.forEach { species ->
                        SuggestionChip(
                            onClick = { onRemoveSpecies(species) },
                            label = { Text(species) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $species"
                                )
                            },
                            enabled = !isSaving
                        )
                    }
                }
            }

            // Species error
            if (formData.errors.containsKey("species")) {
                Text(
                    text = formData.errors["species"]!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Location
        OutlinedTextField(
            value = formData.location,
            onValueChange = { onFieldChange(TankFormField.LOCATION, it) },
            label = { Text("Location (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSaving
        )

        // Status Dropdown
        StatusDropdown(
            selectedStatus = formData.status,
            onStatusChange = onStatusChange,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && formData.isValid
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.width(16.dp).height(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isSaving) "Saving..." else "Save Tank")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selectedStatus: TankStatus,
    onStatusChange: (TankStatus) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatus.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TankStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayName) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TankFormScreenPreview() {
    AquaSenseTheme {
        FormContent(
            formData = TankFormData(
                name = "Main Pond",
                capacity = "5000",
                currentStock = "150",
                species = listOf("Tilapia", "Catfish"),
                location = "Greenhouse A"
            ),
            isSaving = false,
            onFieldChange = { _, _ -> },
            onStatusChange = {},
            onAddSpecies = {},
            onRemoveSpecies = {},
            onSave = {},
            paddingValues = PaddingValues(0.dp)
        )
    }
}
