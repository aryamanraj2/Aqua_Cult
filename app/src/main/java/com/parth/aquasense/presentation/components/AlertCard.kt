package com.parth.aquasense.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parth.aquasense.domain.model.Alert
import com.parth.aquasense.domain.model.AlertType
import com.parth.aquasense.ui.theme.AquaSenseTheme

/**
 * Card displaying an alert with tank name and warning message
 */
@Composable
fun AlertCard(
    alert: Alert,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.type) {
                AlertType.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                AlertType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                AlertType.INFO -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                modifier = Modifier.size(24.dp),
                tint = when (alert.type) {
                    AlertType.CRITICAL -> MaterialTheme.colorScheme.error
                    AlertType.WARNING -> MaterialTheme.colorScheme.tertiary
                    AlertType.INFO -> MaterialTheme.colorScheme.primary
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = alert.tankName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertCardWarningPreview() {
    AquaSenseTheme {
        AlertCard(
            alert = Alert(
                tankId = "1",
                tankName = "Main Pond",
                type = AlertType.WARNING,
                message = "pH level is out of range"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertCardCriticalPreview() {
    AquaSenseTheme {
        AlertCard(
            alert = Alert(
                tankId = "2",
                tankName = "Breeding Tank",
                type = AlertType.CRITICAL,
                message = "Ammonia levels dangerously high"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
