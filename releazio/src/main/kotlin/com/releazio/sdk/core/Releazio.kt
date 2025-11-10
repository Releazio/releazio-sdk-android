package com.releazio.sdk.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.releazio.sdk.models.UpdateState
import com.releazio.sdk.services.ReleaseService
import com.releazio.sdk.services.UpdateStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main entry point for Releazio Android SDK
 */
object Releazio {
    private var configuration: ReleazioConfiguration? = null
    private var context: Context? = null
    private val releaseService by lazy { ReleaseService() }
    private val updateStateManager by lazy { UpdateStateManager() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Configure Releazio SDK with provided configuration
     * @param configuration The configuration object containing API key and other settings
     * @param context Android context (required for some operations)
     */
    fun configure(configuration: ReleazioConfiguration, context: Context) {
        this.configuration = configuration
        this.context = context
        setupServices()

        scope.launch {
            try {
                releaseService.trackEvent("sdk_initialized")
            } catch (e: Exception) {
                if (configuration.debugLoggingEnabled) {
                    Log.w("Releazio", "Failed to track SDK initialization: ${e.message}")
                }
            }
        }

        if (configuration.debugLoggingEnabled) {
            Log.d("Releazio", "✅ Releazio SDK configured successfully")
            Log.d("Releazio", "🔑 API Key: ${configuration.apiKey.take(10)}...")
        }
    }

    /**
     * Check for available updates
     * @return UpdateState with all information about whether to show badges, popups, buttons
     * @throws ReleazioError if configuration is missing or network request fails
     */
    suspend fun checkUpdates(): UpdateState {
        val config = configuration ?: throw ReleazioError.ConfigurationMissing

        try {
            val configResponse = releaseService.getConfig()
            val channelData = configResponse.data.firstOrNull()
                ?: throw ReleazioError.ApiError("NO_CHANNEL_DATA", "No channel data found in config")

            val context = getContext()
            val currentVersionCode = getCurrentVersionCode(context)

            if (config.debugLoggingEnabled) {
                Log.d("Releazio", "📱 Current version code: $currentVersionCode")
                Log.d("Releazio", "📱 Latest version code: ${channelData.appVersionCode}")
                Log.d("Releazio", "📱 Version name: ${channelData.appVersionName}")
            }

            val updateState = updateStateManager.calculateUpdateState(
                channelData = channelData,
                currentVersionCode = currentVersionCode
            )

            scope.launch {
                try {
                    releaseService.trackEvent(
                        "update_checked",
                        mapOf(
                            "has_update" to updateState.isUpdateAvailable.toString(),
                            "current_version" to updateState.currentVersion,
                            "latest_version" to updateState.latestVersion
                        )
                    )
                } catch (e: Exception) {
                    if (config.debugLoggingEnabled) {
                        Log.w("Releazio", "Failed to track update check: ${e.message}")
                    }
                }
            }

            return updateState

        } catch (e: Exception) {
            throw e.asReleazioError()
        }
    }

    /**
     * Mark post as opened (for type 0 badge logic)
     * @param postURL Post URL that was opened
     */
    fun markPostAsOpened(postURL: String) {
        updateStateManager.markPostAsOpened(postURL)
    }

    /**
     * Mark popup as shown (for types 2, 3)
     * @param version Version identifier
     * @param updateType Update type
     */
    fun markPopupAsShown(version: String, updateType: Int) {
        updateStateManager.markPopupAsShown(version, updateType)
    }

    /**
     * Skip update (for type 3)
     * @param version Version identifier
     * @return Remaining skip attempts
     */
    fun skipUpdate(version: String): Int {
        return updateStateManager.skipUpdate(version)
    }

    /**
     * Open App Store for update
     * Opens the update URL from UpdateState (app_url or app_deeplink)
     * @param updateState Update state containing update URL
     * @return True if URL was opened successfully, false otherwise
     */
    fun openAppStore(updateState: UpdateState): Boolean {
        val urlString = updateState.updateURL ?: run {
            if (configuration?.debugLoggingEnabled == true) {
                Log.w("Releazio", "⚠️ No update URL found in update state")
            }
            return false
        }

        return openURL(urlString).also { success ->
            if (configuration?.debugLoggingEnabled == true) {
                Log.d("Releazio", if (success) "✅ Opened App Store URL: $urlString" else "❌ Failed to open App Store URL: $urlString")
            }
        }
    }

    /**
     * Open post URL (for badge click or info button)
     * Opens the post URL from UpdateState (post_url or posts_url)
     * @param updateState Update state containing badge URL
     * @return True if URL was opened successfully, false otherwise
     */
    fun openPostURL(updateState: UpdateState): Boolean {
        val urlString = updateState.badgeURL ?: run {
            if (configuration?.debugLoggingEnabled == true) {
                Log.w("Releazio", "⚠️ No post URL found in update state")
            }
            return false
        }

        val success = openURL(urlString)
        
        if (success && updateState.updateType == 0) {
            markPostAsOpened(postURL = urlString)
        }

        if (configuration?.debugLoggingEnabled == true) {
            Log.d("Releazio", if (success) "✅ Opened post URL: $urlString" else "❌ Failed to open post URL: $urlString")
        }

        return success
    }

    /**
     * Get current configuration
     * @return Current configuration or null if not configured
     */
    fun getConfiguration(): ReleazioConfiguration? = configuration

    /**
     * Reset SDK configuration and clear cached data
     */
    fun reset() {
        configuration = null
        scope.launch {
            try {
                releaseService.clearCache()
                releaseService.trackEvent("sdk_reset")
            } catch (e: Exception) {
                if (configuration?.debugLoggingEnabled == true) {
                    Log.w("Releazio", "Failed to reset services: ${e.message}")
                }
            }
        }
    }

    private fun setupServices() {
        val config = configuration ?: return
        val ctx = context ?: return
        releaseService.configure(config)
        updateStateManager.configure(config, ctx)
    }

    private fun getContext(): Context {
        return context ?: throw ReleazioError.MissingUIContext
    }

    private fun getCurrentVersionCode(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode.toString()
        } catch (e: Exception) {
            "0"
        }
    }

    private fun openURL(urlString: String): Boolean {
        return try {
            val context = getContext()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            if (configuration?.debugLoggingEnabled == true) {
                Log.e("Releazio", "Failed to open URL: $urlString", e)
            }
            false
        }
    }
}
