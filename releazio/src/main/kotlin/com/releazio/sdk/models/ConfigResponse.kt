package com.releazio.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model for getConfig endpoint
 */
@Serializable
data class ConfigResponse(
    /**
     * Home URL for the application
     */
    @SerialName("home_url")
    val homeUrl: String,

    /**
     * Array of channel data
     */
    val data: List<ChannelData>
)

/**
 * Channel data information
 */
@Serializable
data class ChannelData(
    /**
     * Channel type (e.g., "appstore")
     */
    val channel: String,

    /**
     * App version code
     */
    @SerialName("app_version_code")
    val appVersionCode: String,

    /**
     * App version name
     */
    @SerialName("app_version_name")
    val appVersionName: String,

    /**
     * App deep link URL
     */
    @SerialName("app_deeplink")
    val appDeeplink: String? = null,

    /**
     * Channel package name (null for Android)
     */
    @SerialName("channel_package_name")
    val channelPackageName: String? = null,

    /**
     * App store URL
     */
    @SerialName("app_url")
    val appUrl: String? = null,

    /**
     * Post URL
     */
    @SerialName("post_url")
    val postUrl: String? = null,

    /**
     * Posts URL
     */
    @SerialName("posts_url")
    val postsUrl: String? = null,

    /**
     * Update type (0 = latest, 1 = silent, 2 = popup, 3 = popup force)
     */
    @SerialName("update_type")
    val updateType: Int,

    /**
     * Update message
     */
    @SerialName("update_message")
    val updateMessage: String,

    /**
     * Skip attempts count
     */
    @SerialName("skip_attempts")
    val skipAttempts: Int,

    /**
     * Show interval in minutes
     */
    @SerialName("show_interval")
    val showInterval: Int
) {
    /**
     * Whether update is available (any type > 0)
     */
    val hasUpdate: Boolean
        get() = updateType > 0

    /**
     * Whether update type is latest (0) - shows badge only
     */
    val isLatest: Boolean
        get() = updateType == 0

    /**
     * Whether update type is silent (1) - only update button, no popup
     */
    val isSilent: Boolean
        get() = updateType == 1

    /**
     * Whether update type is popup (2) - closable popup
     */
    val isPopup: Boolean
        get() = updateType == 2

    /**
     * Whether update type is popup force (3) - non-closable popup with skip attempts
     */
    val isPopupForce: Boolean
        get() = updateType == 3

    /**
     * Whether update is mandatory (types 2 or 3 require action)
     */
    val isMandatory: Boolean
        get() = updateType == 2 || updateType == 3

    /**
     * Whether update is optional (types 0 or 1)
     */
    val isOptional: Boolean
        get() = updateType == 0 || updateType == 1

    /**
     * Primary download URL
     */
    val primaryDownloadUrl: String?
        get() = appUrl ?: appDeeplink
}

/**
 * Update type enumeration
 */
enum class UpdateType(val value: String) {
    NONE("none"),
    PATCH("patch"),
    MINOR("minor"),
    MAJOR("major"),
    CRITICAL("critical");

    /**
     * Priority for sorting
     */
    val priority: Int
        get() = when (this) {
            NONE -> 0
            PATCH -> 1
            MINOR -> 2
            MAJOR -> 3
            CRITICAL -> 4
        }

    /**
     * Display name
     */
    val displayName: String
        get() = when (this) {
            NONE -> "No Update"
            PATCH -> "Patch"
            MINOR -> "Minor"
            MAJOR -> "Major"
            CRITICAL -> "Critical"
        }
}

/**
 * Update check response
 */
data class UpdateCheckResponse(
    val hasUpdate: Boolean,
    val updateInfo: UpdateInfo?,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String? = null
)

/**
 * Update information for UI display
 */
data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestRelease: Release?,
    val updateType: UpdateType,
    val isMandatory: Boolean
) {
    /**
     * Display priority for sorting
     */
    val priority: Int
        get() = if (isMandatory) 0 else updateType.priority

    /**
     * Should show update prompt
     */
    val shouldShowPrompt: Boolean
        get() = hasUpdate && (isMandatory || updateType == UpdateType.MAJOR || updateType == UpdateType.CRITICAL)

    /**
     * Update message for display
     */
    val updateMessage: String?
        get() = if (!hasUpdate || latestRelease == null) {
            null
        } else if (isMandatory) {
            "A mandatory update is required. Please update to continue using the app."
        } else {
            when (updateType) {
                UpdateType.CRITICAL -> "A critical security update is available."
                UpdateType.MAJOR -> "A major new version with exciting features is available."
                UpdateType.MINOR -> "New features and improvements are available."
                UpdateType.PATCH -> "Bug fixes and improvements are available."
                UpdateType.NONE -> null
            }
        }
}

/**
 * Release information
 */
data class Release(
    val id: String,
    val version: String,
    val buildNumber: String,
    val title: String,
    val description: String?,
    val releaseNotes: String?,
    val releaseDate: Long, // timestamp
    val isMandatory: Boolean,
    val downloadURL: String?
)

/**
 * Analytics event for tracking
 */
data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
