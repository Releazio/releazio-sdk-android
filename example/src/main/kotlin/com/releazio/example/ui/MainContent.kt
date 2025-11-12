package com.releazio.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.releazio.example.MainViewModel
import com.releazio.example.ui.components.HeaderSection
import com.releazio.example.ui.components.QuickActionsGrid
import com.releazio.example.ui.components.UpdateStatusCard
import com.releazio.example.ui.components.BottomVersionView
import com.releazio.example.ui.components.ChangelogDialog
import com.releazio.sdk.Releazio
import com.releazio.sdk.core.ReleazioError
import com.releazio.sdk.core.UpdateCheckConfig
import com.releazio.sdk.models.UpdateState
import kotlinx.coroutines.launch

/**
 * Main content composable - equivalent to ContentView.swift
 */
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var updateState by remember { mutableStateOf<UpdateState?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var showNativeDialog by remember { mutableStateOf(false) }
    var showMaterial3Dialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 100.dp), // Space for bottom version component
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                HeaderSection()
            }

            // Quick Actions Grid
            item {
                QuickActionsGrid(
                    updateState = updateState,
                    isDarkTheme = isDarkTheme,
                    onCheckUpdates = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                // Example: check updates with override config (locale and channel)
                                // val overrideConfig = UpdateCheckConfig(locale = "ru", channel = "playstore")
                                // val state = Releazio.checkUpdates(overrideConfig)
                                
                                // Default: check updates without override
                                val state = Releazio.checkUpdates()
                                updateState = state
                                android.util.Log.d("Example", "✅ Update check completed")
                                android.util.Log.d("Example", "   Current: ${state.currentVersion}")
                                android.util.Log.d("Example", "   Latest: ${state.latestVersion}")
                                android.util.Log.d("Example", "   Available: ${state.isUpdateAvailable}")
                                android.util.Log.d("Example", "   Type: ${state.updateType}")
                            } catch (e: ReleazioError) {
                                errorMessage = e.message
                                android.util.Log.e("Example", "❌ Error: ${e.message}")
                            } catch (e: Exception) {
                                errorMessage = e.message
                                android.util.Log.e("Example", "❌ Error: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onShowChangelog = {
                        showChangelog = true
                    },
                    onShowNativeDialog = { showNativeDialog = true },
                    onShowMaterial3Dialog = { showMaterial3Dialog = true },
                    onToggleTheme = { onThemeChange(!isDarkTheme) }
                )
            }

            // Update Status Card
            updateState?.let { state ->
                item {
                    UpdateStatusCard(updateState = state)
                }
            }

            // Error Message
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Bottom Version View (fixed at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomVersionView(
                updateState = updateState,
                onUpdateTap = {
                    updateState?.let { Releazio.openAppStore(it) }
                }
            )
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Checking for updates...")
                    }
                }
            }
        }
    }

    // Dialogs
    if (showChangelog) {
        ChangelogDialog(
            updateState = updateState,
            onDismiss = { showChangelog = false }
        )
    }

    if (showNativeDialog && updateState != null) {
        // Native style dialog would go here
        AlertDialog(
            onDismissRequest = { showNativeDialog = false },
            title = { Text("Update Available") },
            text = { Text("A new version is available. Would you like to update?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        updateState?.let { Releazio.openAppStore(it) }
                        showNativeDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNativeDialog = false }) {
                    Text("Later")
                }
            }
        )
    }

    if (showMaterial3Dialog && updateState != null) {
        // Material3 style dialog would go here
        AlertDialog(
            onDismissRequest = { showMaterial3Dialog = false },
            title = { Text("Update Available") },
            text = { Text("A new version is available. Would you like to update?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        updateState?.let { Releazio.openAppStore(it) }
                        showMaterial3Dialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMaterial3Dialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}
