package com.releazio.example.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.releazio.sdk.models.UpdateState

/**
 * Bottom version view - equivalent to VersionView in iOS
 * Shows current version and yellow update button if available
 */
@Composable
fun BottomVersionView(
    updateState: UpdateState?,
    onUpdateTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Get current version from package
    val currentVersion = try {
        val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = pkgInfo.versionName ?: "1.0.0"
        val versionCode = pkgInfo.versionCode
        "Version $versionName ($versionCode)"
    } catch (e: PackageManager.NameNotFoundException) {
        "Version 1.0.0 (1)"
    }
    
    // Gradient fade effect at the top
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 34.dp) // Space above system navigation bar
    ) {
        // Gradient fade effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )
        
        // Version component (compact, like iOS)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Version text on the left
                Text(
                    text = currentVersion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Yellow update button on the right (if update available)
                if (updateState != null && updateState.isUpdateAvailable) {
                    Button(
                        onClick = onUpdateTap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700), // Yellow like iOS
                            contentColor = Color.Black
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Update",
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Show "Up to date" if no update
                    Text(
                        text = "Up to date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
