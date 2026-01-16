package com.parth.aquasense.presentation.screens.waterqualityform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Water Quality Form Screen
 * Allows adding new water quality readings for a tank
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterQualityFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: WaterQualityFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back after successful save
    LaunchedEffect(uiState) {
        if (uiState is WaterQualityFormUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            WaterQualityFormTopBar(
                tankName = when (val state = uiState) {
                    is WaterQualityFormUiState.Ready -> state.tankName
                    else -> ""
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is WaterQualityFormUiState.Loading -> {
                LoadingContent(paddingValues)
            }

            is WaterQualityFormUiState.Ready -> {
                FormContent(
                    formData = state.formData,
                    isSaving = state.isSaving,
                    onFieldChange = viewModel::updateField,
                    onSave = viewModel::saveReading,
                    paddingValues = paddingValues
                )
            }

            is WaterQualityFormUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    paddingValues = paddingValues
                )
            }

            is WaterQualityFormUiState.Success -> {
                // Navigation handled by LaunchedEffect
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterQualityFormTopBar(
    tankName: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text("Add Water Quality Reading")
                if (tankName.isNotEmpty()) {
                    Text(
                        text = "Tank: $tankName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
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
    formData: WaterQualityFormData,
    isSaving: Boolean,
    onFieldChange: (WaterQualityFormField, String) -> Unit,
    onSave: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Required Parameters Section
        Text(
            text = "Required Parameters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // pH
        OutlinedTextField(
            value = formData.ph,
            onValueChange = { onFieldChange(WaterQualityFormField.PH, it) },
            label = { Text("pH *") },
            placeholder = { Text("0-14") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("ph"),
            supportingText = formData.errors["ph"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Temperature
        OutlinedTextField(
            value = formData.temperature,
            onValueChange = { onFieldChange(WaterQualityFormField.TEMPERATURE, it) },
            label = { Text("Temperature (°C) *") },
            placeholder = { Text("-10 to 50") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("temperature"),
            supportingText = formData.errors["temperature"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Dissolved Oxygen
        OutlinedTextField(
            value = formData.dissolvedOxygen,
            onValueChange = { onFieldChange(WaterQualityFormField.DISSOLVED_OXYGEN, it) },
            label = { Text("Dissolved Oxygen (mg/L) *") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("dissolved_oxygen"),
            supportingText = formData.errors["dissolved_oxygen"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Parameters Section
        Text(
            text = "Optional Parameters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        // Ammonia
        OutlinedTextField(
            value = formData.ammonia,
            onValueChange = { onFieldChange(WaterQualityFormField.AMMONIA, it) },
            label = { Text("Ammonia (mg/L)") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("ammonia"),
            supportingText = formData.errors["ammonia"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Nitrite
        OutlinedTextField(
            value = formData.nitrite,
            onValueChange = { onFieldChange(WaterQualityFormField.NITRITE, it) },
            label = { Text("Nitrite (mg/L)") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("nitrite"),
            supportingText = formData.errors["nitrite"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Nitrate
        OutlinedTextField(
            value = formData.nitrate,
            onValueChange = { onFieldChange(WaterQualityFormField.NITRATE, it) },
            label = { Text("Nitrate (mg/L)") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("nitrate"),
            supportingText = formData.errors["nitrate"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Salinity
        OutlinedTextField(
            value = formData.salinity,
            onValueChange = { onFieldChange(WaterQualityFormField.SALINITY, it) },
            label = { Text("Salinity (ppt)") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("salinity"),
            supportingText = formData.errors["salinity"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        // Turbidity
        OutlinedTextField(
            value = formData.turbidity,
            onValueChange = { onFieldChange(WaterQualityFormField.TURBIDITY, it) },
            label = { Text("Turbidity (NTU)") },
            placeholder = { Text("0 or greater") },
            modifier = Modifier.fillMaxWidth(),
            isError = formData.errors.containsKey("turbidity"),
            supportingText = formData.errors["turbidity"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = formData.isValid && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(if (isSaving) "Saving..." else "Save Reading")
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
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
