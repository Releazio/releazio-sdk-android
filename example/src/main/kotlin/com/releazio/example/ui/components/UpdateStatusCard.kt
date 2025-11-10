package com.releazio.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.releazio.sdk.models.UpdateState

/**
 * Update status card showing information from UpdateState
 */
@Composable
fun UpdateStatusCard(
    updateState: UpdateState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Update Status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Main status
            StatusRow(
                icon = if (updateState.isUpdateAvailable) Icons.Default.ArrowUpward else Icons.Default.CheckCircle,
                color = if (updateState.isUpdateAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                title = if (updateState.isUpdateAvailable) "Update Available" else "Up to Date",
                subtitle = "Current: ${updateState.currentVersionName} (${updateState.currentVersion}) → Latest: ${updateState.latestVersionName} (${updateState.latestVersion})"
            )

            // Badge status
            if (updateState.shouldShowBadge) {
                StatusRow(
                    icon = Icons.Default.Circle,
                    color = MaterialTheme.colorScheme.error,
                    title = "Badge",
                    subtitle = "New post available"
                )
            }

            // Popup status
            if (updateState.shouldShowPopup) {
                StatusRow(
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.error,
                    title = "Popup",
                    subtitle = "Type ${updateState.updateType} - should show popup"
                )
            }

            // Update button status
            if (updateState.shouldShowUpdateButton) {
                StatusRow(
                    icon = Icons.Default.Download,
                    color = MaterialTheme.colorScheme.primary,
                    title = "Update Button",
                    subtitle = "Type ${updateState.updateType} - show update button"
                )
            }

            // Skip attempts (for type 3)
            if (updateState.updateType == 3 && updateState.remainingSkipAttempts > 0) {
                StatusRow(
                    icon = Icons.Default.SkipNext,
                    color = MaterialTheme.colorScheme.outline,
                    title = "Skip Attempts",
                    subtitle = "Remaining: ${updateState.remainingSkipAttempts}"
                )
            }
        }
    }
}

/**
 * Individual status row
 */
@Composable
private fun StatusRow(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
