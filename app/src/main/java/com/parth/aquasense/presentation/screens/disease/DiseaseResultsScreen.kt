package com.parth.aquasense.presentation.screens.disease

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.DiseaseInfo
import com.parth.aquasense.domain.model.DiseaseSeverity
import com.parth.aquasense.presentation.components.FormattedText

/**
 * Disease Results Screen
 *
 * Displays the top detected disease with:
 * - Disease name and confidence
 * - Severity badge
 * - Description
 * - Causes, symptoms, treatment, prevention
 * - Overall recommendation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiseaseDetectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val detection = when (val state = uiState) {
        is DiseaseDetectionUiState.Success -> state.detection
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Detection Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        if (detection == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("No detection data available")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Tank info
            Text(
                text = "Tank: ${detection.tankName}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Severity badge
            SeverityBadge(severity = detection.severity)

            if (detection.urgentActionRequired) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Urgent Action Required",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            HorizontalDivider()

            // Top disease
            detection.topDisease?.let { disease ->
                DiseaseDetailsCard(disease = disease)
            }

            // Overall recommendation
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    FormattedText(
                        text = detection.recommendation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Done button
            Button(
                onClick = {
                    viewModel.resetState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }

            // Bottom spacer to ensure content doesn't get cut off by bottom nav
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SeverityBadge(severity: DiseaseSeverity) {
    val (color, text) = when (severity) {
        DiseaseSeverity.LOW -> Color(0xFF4CAF50) to "Low Severity"
        DiseaseSeverity.MEDIUM -> Color(0xFFFFC107) to "Medium Severity"
        DiseaseSeverity.HIGH -> Color(0xFFFF9800) to "High Severity"
        DiseaseSeverity.CRITICAL -> Color(0xFFF44336) to "Critical Severity"
    }

    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}

@Composable
fun DiseaseDetailsCard(disease: DiseaseInfo) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Disease name
            Text(
                text = disease.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${(disease.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { disease.confidence.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            // Description
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleSmall
            )
            FormattedText(
                text = disease.description,
                style = MaterialTheme.typography.bodyMedium
            )

            // Causes
            if (disease.causes.isNotEmpty()) {
                Text(
                    text = "Causes",
                    style = MaterialTheme.typography.titleSmall
                )
                disease.causes.forEach { cause ->
                    Text(
                        text = "• $cause",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Symptoms
            if (disease.symptoms.isNotEmpty()) {
                Text(
                    text = "Symptoms",
                    style = MaterialTheme.typography.titleSmall
                )
                disease.symptoms.forEach { symptom ->
                    Text(
                        text = "• $symptom",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            // Treatment
            Text(
                text = "Treatment",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            FormattedText(
                text = disease.treatment,
                style = MaterialTheme.typography.bodyMedium
            )

            // Prevention
            if (disease.prevention.isNotEmpty()) {
                Text(
                    text = "Prevention",
                    style = MaterialTheme.typography.titleSmall
                )
                disease.prevention.forEach { prevention ->
                    Text(
                        text = "• $prevention",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
