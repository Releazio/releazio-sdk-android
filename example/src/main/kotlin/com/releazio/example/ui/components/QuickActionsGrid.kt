package com.releazio.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.releazio.sdk.models.UpdateState

/**
 * Quick actions grid with buttons for different SDK functions
 */
@Composable
fun QuickActionsGrid(
    updateState: UpdateState?,
    isDarkTheme: Boolean,
    onCheckUpdates: () -> Unit,
    onShowChangelog: () -> Unit,
    onShowNativeDialog: () -> Unit,
    onShowMaterial3Dialog: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        val actionItems = getActionItems(
            updateState = updateState,
            isDarkTheme = isDarkTheme,
            onCheckUpdates = onCheckUpdates,
            onShowChangelog = onShowChangelog,
            onShowNativeDialog = onShowNativeDialog,
            onShowMaterial3Dialog = onShowMaterial3Dialog,
            onToggleTheme = onToggleTheme
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(actionItems) { item ->
                ActionButton(
                    title = item.title,
                    icon = item.icon,
                    color = item.color,
                    onClick = item.action
                )
            }
        }
    }
}

/**
 * Action item data class
 */
private data class ActionItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val action: () -> Unit
)

/**
 * Get list of action items based on current state
 */
@Composable
private fun getActionItems(
    updateState: UpdateState?,
    isDarkTheme: Boolean,
    onCheckUpdates: () -> Unit,
    onShowChangelog: () -> Unit,
    onShowNativeDialog: () -> Unit,
    onShowMaterial3Dialog: () -> Unit,
    onToggleTheme: () -> Unit
): List<ActionItem> {
    val items = mutableListOf<ActionItem>()

    // Always available actions
    items.add(
        ActionItem(
            title = "Check Updates",
            icon = Icons.Default.Refresh,
            color = MaterialTheme.colorScheme.primary,
            action = onCheckUpdates
        )
    )

    items.add(
        ActionItem(
            title = "Show Changelog",
            icon = Icons.Default.Description,
            color = MaterialTheme.colorScheme.secondary,
            action = onShowChangelog
        )
    )

    // Conditional actions based on update state
    updateState?.let { state ->
        if (state.shouldShowPopup) {
            items.add(
                ActionItem(
                    title = "Show Native Dialog",
                    icon = Icons.Default.Notifications,
                    color = MaterialTheme.colorScheme.tertiary,
                    action = onShowNativeDialog
                )
            )

            items.add(
                ActionItem(
                    title = "Show Material3 Dialog",
                    icon = Icons.Default.Palette,
                    color = MaterialTheme.colorScheme.error,
                    action = onShowMaterial3Dialog
                )
            )
        }
    }

    // Theme toggle
    items.add(
        ActionItem(
            title = if (isDarkTheme) "Switch to Light" else "Switch to Dark",
            icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            color = MaterialTheme.colorScheme.outline,
            action = onToggleTheme
        )
    )

    return items
}

/**
 * Individual action button
 */
@Composable
private fun ActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
