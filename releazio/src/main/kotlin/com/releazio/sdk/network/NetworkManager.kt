package com.releazio.sdk.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.releazio.sdk.core.ReleazioConfiguration
import com.releazio.sdk.core.ReleazioError
import com.releazio.sdk.core.asReleazioError
import com.releazio.sdk.models.*
import com.releazio.sdk.utils.AppStoreHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/**
 * Protocol for network manager
 */
interface NetworkManagerProtocol {
    suspend fun getConfig(locale: String? = null, channel: String? = null): ConfigResponse
    suspend fun getReleases(): List<Release>
    suspend fun getLatestRelease(): Release?
    suspend fun getChangelog(): List<ChangelogEntry>
    suspend fun getPostContent(url: String): String
    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResponse
    suspend fun trackEvent(event: AnalyticsEvent)
    suspend fun validateAPIKey(): Boolean
}

/**
 * Network manager for coordinating API requests
 */
class NetworkManager(
    private val configuration: ReleazioConfiguration,
    private val context: Context,
    private val networkClient: NetworkClientProtocol? = null
) : NetworkManagerProtocol {

    /**
     * Network client instance
     */
    private val client = networkClient ?: NetworkClient(configuration)

    /**
     * Request cache for handling concurrent requests
     */
    private val requestCache = mutableMapOf<String, Any>()
    private val cacheMutex = Mutex()

    /**
     * Get application configuration
     * @param locale Optional locale override
     * @param channel Optional channel override
     * @return Configuration response
     * @throws ReleazioError
     */
    override suspend fun getConfig(locale: String?, channel: String?): ConfigResponse {
        // Collect device and app information
        val packageManager = context.packageManager
        val packageInfo = try {
            packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw ReleazioError.ApiError("PACKAGE_NOT_FOUND", "Package not found: ${context.packageName}")
        }

        // Determine channel
        val detectedChannel = channel ?: run {
            val primaryStore = AppStoreHelper.detectPrimaryStore(context)
            AppStoreHelper.getChannelFromStore(primaryStore)
        }

        // Collect app information
        val appId = context.packageName
        val appVersionCode = packageInfo.longVersionCode.toString()
        val appVersionName = packageInfo.versionName ?: "1.0.0"

        // Collect locale information (use override if provided)
        val defaultLocale = Locale.getDefault()
        val phoneLocaleCountry = defaultLocale.country
        val phoneLocaleLanguage = defaultLocale.language

        // Collect OS information
        val osVersionCode = Build.VERSION.SDK_INT

        // Collect device information
        val deviceManufacturer = Build.MANUFACTURER
        val deviceBrand = Build.BRAND
        val deviceModel = Build.MODEL

        // Build endpoint URL with all query parameters
        val endpoint = APIEndpoints.getConfig(
            channel = detectedChannel,
            appId = appId,
            appVersionCode = appVersionCode,
            appVersionName = appVersionName,
            phoneLocaleCountry = phoneLocaleCountry,
            phoneLocaleLanguage = phoneLocaleLanguage,
            osVersionCode = osVersionCode,
            deviceManufacturer = deviceManufacturer,
            deviceBrand = deviceBrand,
            deviceModel = deviceModel
        )

        return try {
            val request = APIRequestBuilder.get(
                url = endpoint,
                headers = getDefaultHeaders(),
                timeout = configuration.networkTimeout
            )
            client.request<ConfigResponse>(request)
        } catch (e: Exception) {
            throw e.asReleazioError()
        }
    }

    /**
     * Get all releases for an application (extracted from config)
     * @return Array of releases
     * @throws ReleazioError
     */
    override suspend fun getReleases(): List<Release> {
        val config = getConfig()
        
        // Convert channel data to releases
        return config.data.map { channelData ->
            Release(
                id = channelData.channel,
                version = channelData.appVersionName,
                buildNumber = channelData.appVersionCode,
                title = "Version ${channelData.appVersionName}",
                description = if (channelData.updateMessage.isNotEmpty()) channelData.updateMessage else null,
                releaseNotes = if (channelData.updateMessage.isNotEmpty()) channelData.updateMessage else null,
                releaseDate = System.currentTimeMillis(),
                isMandatory = channelData.isMandatory,
                downloadURL = channelData.primaryDownloadUrl
            )
        }
    }

    /**
     * Get latest release for an application (extracted from config)
     * @return Latest release or null if no releases
     * @throws ReleazioError
     */
    override suspend fun getLatestRelease(): Release? {
        val releases = getReleases()
        return releases.firstOrNull()
    }

    /**
     * Get changelog for an application (extracted from config)
     * @return Array of changelog entries
     * @throws ReleazioError
     */
    override suspend fun getChangelog(): List<ChangelogEntry> {
        val config = getConfig()
        
        // Convert channel data to changelog entries
        return config.data.mapNotNull { channelData ->
            if (channelData.updateMessage.isEmpty()) return@mapNotNull null
            
            ChangelogEntry(
                id = channelData.channel,
                title = "Version ${channelData.appVersionName}",
                description = channelData.updateMessage,
                category = ChangelogCategory.FEATURE,
                priority = if (channelData.isMandatory) ChangelogPriority.CRITICAL else ChangelogPriority.NORMAL,
                tags = emptyList(),
                isBreaking = false
            )
        }
    }
    
    /**
     * Get post content from URL
     * @param url Post URL
     * @return Post content as string
     * @throws ReleazioError
     */
    override suspend fun getPostContent(url: String): String {
        val postURL = java.net.URL(url)
        
        return try {
            val request = APIRequestBuilder.get(
                url = postURL,
                headers = getDefaultHeaders(),
                timeout = configuration.networkTimeout
            )
            client.request<String>(request)
        } catch (e: Exception) {
            throw e.asReleazioError()
        }
    }

    /**
     * Check for updates (extracted from config)
     * @param currentVersion Current app version
     * @return Update check response
     * @throws ReleazioError
     */
    override suspend fun checkForUpdates(currentVersion: String): UpdateCheckResponse {
        val config = getConfig()
        
        // Find the first channel data (usually Play Store)
        val channelData = config.data.firstOrNull() ?: return UpdateCheckResponse(
            hasUpdate = false,
            updateInfo = null,
            maintenanceMode = false,
            maintenanceMessage = null
        )
        
        // Debug logging
        android.util.Log.d("Releazio", "🔍 API Response:")
        android.util.Log.d("Releazio", "   Channel data: $channelData")
        android.util.Log.d("Releazio", "   App version name: ${channelData.appVersionName}")
        android.util.Log.d("Releazio", "   App version code: ${channelData.appVersionCode}")
        android.util.Log.d("Releazio", "   Has update: ${channelData.hasUpdate}")
        android.util.Log.d("Releazio", "   Is mandatory: ${channelData.isMandatory}")
        android.util.Log.d("Releazio", "   Update type: ${channelData.updateType}")
        
        val hasUpdate = channelData.hasUpdate
        val isMandatory = channelData.isMandatory
        
        val updateType = when (channelData.updateType) {
            0 -> UpdateType.NONE
            1 -> UpdateType.MINOR
            2 -> UpdateType.MAJOR
            3 -> UpdateType.CRITICAL
            else -> UpdateType.MINOR
        }
        
        val latestRelease = Release(
            id = channelData.channel,
            version = channelData.appVersionName,
            buildNumber = channelData.appVersionCode,
            title = "Version ${channelData.appVersionName}",
            description = if (channelData.updateMessage.isEmpty()) null else channelData.updateMessage,
            releaseNotes = if (channelData.updateMessage.isEmpty()) null else channelData.updateMessage,
            releaseDate = System.currentTimeMillis(),
            isMandatory = isMandatory,
            downloadURL = channelData.primaryDownloadUrl
        )
        
        val updateInfo = UpdateInfo(
            hasUpdate = hasUpdate,
            latestRelease = latestRelease,
            updateType = updateType,
            isMandatory = isMandatory
        )
        
        return UpdateCheckResponse(
            hasUpdate = hasUpdate,
            updateInfo = updateInfo,
            maintenanceMode = false,
            maintenanceMessage = null
        )
    }

    /**
     * Track analytics event
     * @param event Analytics event
     * @throws ReleazioError
     */
    override suspend fun trackEvent(event: AnalyticsEvent) {
        if (configuration.debugLoggingEnabled) {
            android.util.Log.d("Releazio", "📊 Analytics Event: ${event.name} - ${event.properties}")
        }
    }

    /**
     * Validate API key
     * @return True if API key is valid
     * @throws ReleazioError if network request fails
     */
    override suspend fun validateAPIKey(): Boolean {
        return try {
            getConfig()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get default headers for all requests
     * @return Default headers map
     */
    private fun getDefaultHeaders(): Map<String, String> {
        return mapOf(
            "Accept" to "application/json"
        )
    }

    /**
     * Execute request with caching to prevent duplicate concurrent requests
     * @param key Cache key
     * @param operation Async operation to execute
     * @return Result of the operation
     */
    private suspend fun <T> withCaching(
        key: String,
        operation: suspend () -> T
    ): T {
        return cacheMutex.withLock {
            if (requestCache.containsKey(key)) {
                @Suppress("UNCHECKED_CAST")
                requestCache[key] as T
            } else {
                val result = operation()
                requestCache[key] = result as Any
                result
            }
        }
    }
}

/**
 * Changelog entry
 */
data class ChangelogEntry(
    val id: String,
    val title: String,
    val description: String,
    val category: ChangelogCategory,
    val priority: ChangelogPriority,
    val tags: List<String>,
    val isBreaking: Boolean
)

/**
 * Changelog category
 */
enum class ChangelogCategory {
    FEATURE,
    BUGFIX,
    IMPROVEMENT,
    SECURITY,
    BREAKING
}

/**
 * Changelog priority
 */
enum class ChangelogPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}
