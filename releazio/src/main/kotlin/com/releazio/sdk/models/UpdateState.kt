package com.releazio.sdk.models

/**
 * State returned by checkUpdates() method
 */
data class UpdateState(
    /**
     * Update type from API (0, 1, 2, 3)
     */
    val updateType: Int,

    /**
     * Whether badge should be shown (for type 0)
     */
    val shouldShowBadge: Boolean,

    /**
     * Whether popup should be shown (for types 2, 3)
     */
    val shouldShowPopup: Boolean,

    /**
     * Whether update button should be shown (for type 1)
     */
    val shouldShowUpdateButton: Boolean,

    /**
     * Remaining skip attempts (for type 3)
     */
    val remainingSkipAttempts: Int,

    /**
     * Full channel data from API
     */
    val channelData: ChannelData,

    /**
     * URL to open when badge is clicked (post_url or posts_url)
     */
    val badgeURL: String?,

    /**
     * URL for app update (app_url)
     */
    val updateURL: String?,

    /**
     * Current app version code (for comparison)
     */
    val currentVersion: String,

    /**
     * Latest available version code from API (for comparison)
     */
    val latestVersion: String,

    /**
     * Current app version name (for display, e.g., "1.2.3")
     */
    val currentVersionName: String,

    /**
     * Latest available version name from API (for display, e.g., "2.5.1")
     */
    val latestVersionName: String,

    /**
     * Whether update is available (version comparison)
     */
    val isUpdateAvailable: Boolean
) {
    /**
     * Get display-friendly update type name
     */
    val updateTypeName: String
        get() = when (updateType) {
            0 -> "Latest"
            1 -> "Silent"
            2 -> "Popup"
            3 -> "Popup Force"
            else -> "Unknown"
        }

    /**
     * Check if this is a mandatory update
     */
    val isMandatory: Boolean
        get() = updateType == 2 || updateType == 3

    /**
     * Check if this is an optional update
     */
    val isOptional: Boolean
        get() = updateType == 0 || updateType == 1
}
