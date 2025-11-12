package com.releazio.sdk.services

import com.releazio.sdk.core.ReleazioConfiguration
import com.releazio.sdk.core.ReleazioError
import com.releazio.sdk.core.asReleazioError
import com.releazio.sdk.models.*
import com.releazio.sdk.network.NetworkManager
import com.releazio.sdk.network.NetworkManagerProtocol
import com.releazio.sdk.network.ChangelogEntry
import com.releazio.sdk.network.ChangelogCategory
import com.releazio.sdk.network.ChangelogPriority

/**
 * Protocol for release service
 */
interface ReleaseServiceProtocol {
    suspend fun getReleases(): List<Release>
    suspend fun getLatestRelease(): Release?
    suspend fun getRelease(releaseId: String): Release
    suspend fun getChangelog(releaseId: String): Changelog
    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResponse
    suspend fun getUpdateInfo(currentVersion: String): UpdateInfo
    suspend fun getConfig(): ConfigResponse
    suspend fun clearCache()
    suspend fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap())
}

/**
 * Service for managing app releases and updates
 */
class ReleaseService : ReleaseServiceProtocol {
    private var networkManager: NetworkManagerProtocol? = null
    private var configuration: ReleazioConfiguration? = null
    private var cacheService: CacheService? = null

    /**
     * Configure service with dependencies
     * @param configuration Releazio configuration
     */
    fun configure(configuration: ReleazioConfiguration) {
        this.configuration = configuration

        if (networkManager == null) {
            this.networkManager = NetworkManager(configuration)
        }

        if (cacheService == null) {
            this.cacheService = CacheService(configuration)
        }
    }

    /**
     * Get all releases for an application
     * @return Array of releases sorted by version (newest first)
     * @throws ReleazioError
     */
    override suspend fun getReleases(): List<Release> {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing

        return try {
            val releases = networkManager.getReleases()
            trackEvent("releases_fetched", mapOf("count" to releases.size))
            releases.sortedByDescending { it.version }
        } catch (e: Exception) {
            trackEvent("request_failed", mapOf("error" to (e.message ?: "Unknown error")))
            throw e.asReleazioError()
        }
    }

    /**
     * Get latest release for an application
     * @return Latest release or null if no releases
     * @throws ReleazioError
     */
    override suspend fun getLatestRelease(): Release? {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing

        return try {
            val release = networkManager.getLatestRelease()
            if (release != null) {
                trackEvent("latest_release_fetched", mapOf("version" to release.version))
            }
            release
        } catch (e: Exception) {
            trackEvent("request_failed", mapOf("error" to (e.message ?: "Unknown error")))
            throw e.asReleazioError()
        }
    }

    /**
     * Get specific release by ID
     * @param releaseId Release identifier
     * @return Release information
     * @throws ReleazioError
     */
    override suspend fun getRelease(releaseId: String): Release {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing

        return try {
            val release = networkManager.getLatestRelease()
                ?: throw ReleazioError.ApiError("RELEASE_NOT_FOUND", "Release not found")

            trackEvent("release_fetched", mapOf("id" to releaseId))
            release
        } catch (e: Exception) {
            trackEvent("request_failed", mapOf("error" to (e.message ?: "Unknown error")))
            throw e.asReleazioError()
        }
    }

    /**
     * Get changelog for a release
     * @param releaseId Release identifier
     * @return Changelog information
     * @throws ReleazioError
     */
    override suspend fun getChangelog(releaseId: String): Changelog {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing

        return try {
            val config = networkManager.getConfig()
            val channelData = config.data.firstOrNull()
            
            var content = ""
            var changelogEntries: List<ChangelogEntry> = emptyList()
            
            if (channelData?.postUrl != null) {
                if (configuration?.debugLoggingEnabled == true) {
                    android.util.Log.d("Releazio", "🔗 Using post URL: ${channelData.postUrl}")
                }
                content = channelData.postUrl
            } else {
                if (configuration?.debugLoggingEnabled == true) {
                    android.util.Log.w("Releazio", "⚠️ No post URL found, using update message")
                }
                content = channelData?.updateMessage ?: "No changelog available"
            }
            
            if (content.isNotEmpty()) {
                val entry = ChangelogEntry(
                    id = releaseId,
                    title = "Version ${channelData?.appVersionName ?: "Unknown"}",
                    description = content,
                    category = ChangelogCategory.FEATURE,
                    priority = if (channelData?.isMandatory == true) ChangelogPriority.CRITICAL else ChangelogPriority.NORMAL,
                    tags = emptyList(),
                    isBreaking = false
                )
                changelogEntries = listOf(entry)
            }

            val changelog = Changelog(
                id = releaseId,
                releaseId = releaseId,
                title = "Changelog for Release $releaseId",
                content = content,
                entries = changelogEntries,
                categories = emptyList(),
                author = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                publishedAt = System.currentTimeMillis(),
                locale = "en",
                isPublic = true
            )

            trackEvent("changelog_fetched", mapOf("release_id" to releaseId))
            changelog
        } catch (e: Exception) {
            trackEvent("request_failed", mapOf("error" to (e.message ?: "Unknown error")))
            throw e.asReleazioError()
        }
    }

    /**
     * Check for updates
     * @param currentVersion Current app version
     * @return Update check response
     * @throws ReleazioError
     */
    override suspend fun checkForUpdates(currentVersion: String): UpdateCheckResponse {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing

        return try {
            val updateResponse = networkManager.checkForUpdates(currentVersion)
            trackEvent(
                "update_checked",
                mapOf(
                    "has_update" to updateResponse.hasUpdate.toString(),
                    "current_version" to currentVersion,
                    "latest_version" to (updateResponse.updateInfo?.latestRelease?.version ?: "unknown")
                )
            )
            updateResponse
        } catch (e: Exception) {
            trackEvent("request_failed", mapOf("error" to (e.message ?: "Unknown error")))
            throw e.asReleazioError()
        }
    }

    /**
     * Clear all cached data
     */
    override suspend fun clearCache() {
        trackEvent("cache_cleared")
    }
    
    /**
     * Get configuration from API
     * @param locale Optional locale override
     * @param channel Optional channel override
     * @return Configuration response
     * @throws ReleazioError
     */
    override suspend fun getConfig(): ConfigResponse {
        return getConfig(locale = null, channel = null)
    }

    /**
     * Get configuration from API with override parameters
     * @param locale Optional locale override
     * @param channel Optional channel override
     * @return Configuration response
     * @throws ReleazioError
     */
    suspend fun getConfig(locale: String? = null, channel: String? = null): ConfigResponse {
        val networkManager = this.networkManager ?: throw ReleazioError.ConfigurationMissing
        return networkManager.getConfig(locale = locale, channel = channel)
    }

    /**
     * Get update information for UI display
     * @param currentVersion Current version
     * @return Update information
     * @throws ReleazioError
     */
    override suspend fun getUpdateInfo(currentVersion: String): UpdateInfo {
        val updateResponse = checkForUpdates(currentVersion)

        if (!updateResponse.hasUpdate || updateResponse.updateInfo?.latestRelease == null) {
            return UpdateInfo(
                hasUpdate = false,
                latestRelease = null,
                updateType = UpdateType.NONE,
                isMandatory = false
            )
        }

        val latestRelease = updateResponse.updateInfo.latestRelease
        val updateType = updateResponse.updateInfo.updateType

        return UpdateInfo(
            hasUpdate = true,
            latestRelease = latestRelease,
            updateType = updateType,
            isMandatory = updateResponse.updateInfo.isMandatory || latestRelease.isMandatory
        )
    }

    /**
     * Track analytics event
     * @param eventName Event name
     * @param properties Event properties
     */
    override suspend fun trackEvent(eventName: String, properties: Map<String, Any>) {
        try {
            val event = AnalyticsEvent(
                name = eventName,
                properties = properties,
                timestamp = System.currentTimeMillis()
            )
            networkManager?.trackEvent(event)
        } catch (e: Exception) {
            if (configuration?.debugLoggingEnabled == true) {
                android.util.Log.w("Releazio", "Failed to track event: $eventName", e)
            }
        }
    }
}

/**
 * Changelog model
 */
data class Changelog(
    val id: String,
    val releaseId: String,
    val title: String,
    val content: String,
    val entries: List<ChangelogEntry>,
    val categories: List<String>,
    val author: Author?,
    val createdAt: Long,
    val updatedAt: Long,
    val publishedAt: Long,
    val locale: String,
    val isPublic: Boolean
)

/**
 * Author model
 */
data class Author(
    val name: String,
    val role: String
)
