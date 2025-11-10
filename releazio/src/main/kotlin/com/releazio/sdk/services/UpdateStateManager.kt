package com.releazio.sdk.services

import com.releazio.sdk.core.ReleazioConfiguration
import com.releazio.sdk.models.ChannelData
import com.releazio.sdk.models.UpdateState
import com.releazio.sdk.utils.VersionComparator

/**
 * Manager for determining update state and UI visibility
 */
class UpdateStateManager(
    private val storage: UpdateStorage? = null
) {
    private var configuration: ReleazioConfiguration? = null
    private var actualStorage: UpdateStorage? = storage

    /**
     * Configure the manager
     * @param configuration Releazio configuration
     * @param context Android context (required if storage is not provided)
     */
    fun configure(configuration: ReleazioConfiguration, context: android.content.Context? = null) {
        this.configuration = configuration
        if (actualStorage == null && context != null) {
            actualStorage = UpdateStorage(context)
        }
    }

    /**
     * Calculate update state from channel data
     * @param channelData Channel data from API
     * @param currentVersionCode Current app version code from context
     * @return UpdateState with all visibility flags
     */
    fun calculateUpdateState(
        channelData: ChannelData,
        currentVersionCode: String
    ): UpdateState {
        val isUpdateAvailable = VersionComparator.isNewerVersion(
            channelData.appVersionCode,
            currentVersionCode
        )
        
        if (configuration?.debugLoggingEnabled == true) {
            android.util.Log.d("Releazio", "🔍 Version comparison:")
            android.util.Log.d("Releazio", "   Current: $currentVersionCode")
            android.util.Log.d("Releazio", "   Latest: ${channelData.appVersionCode}")
            android.util.Log.d("Releazio", "   Update available: $isUpdateAvailable")
        }
        
        val shouldShowBadge = shouldShowBadgeForType0(
            channelData = channelData,
            isUpdateAvailable = isUpdateAvailable
        )
        
        val shouldShowPopup = shouldShowPopupForTypes2and3(
            channelData = channelData,
            isUpdateAvailable = isUpdateAvailable
        )
        
        val shouldShowUpdateButton = shouldShowUpdateButtonForType1(
            channelData = channelData,
            isUpdateAvailable = isUpdateAvailable
        )
        
        val remainingSkipAttempts = getRemainingSkipAttempts(channelData)
        val badgeURL = getBadgeURL(channelData)
        val currentVersionName = currentVersionCode
        val latestVersionName = channelData.appVersionName
        
        return UpdateState(
            updateType = channelData.updateType,
            shouldShowBadge = shouldShowBadge,
            shouldShowPopup = shouldShowPopup,
            shouldShowUpdateButton = shouldShowUpdateButton,
            remainingSkipAttempts = remainingSkipAttempts,
            channelData = channelData,
            badgeURL = badgeURL,
            updateURL = channelData.appUrl ?: channelData.appDeeplink,
            currentVersion = currentVersionCode,
            latestVersion = channelData.appVersionCode,
            currentVersionName = currentVersionName,
            latestVersionName = latestVersionName,
            isUpdateAvailable = isUpdateAvailable
        )
    }
    
    /**
     * Mark post as opened (for type 0 badge logic)
     * @param postURL Post URL to mark as opened
     */
    fun markPostAsOpened(postURL: String) {
        actualStorage?.markPostAsOpened(postURL)
    }
    
    /**
     * Mark popup as shown (for type 2, 3)
     * @param version Version identifier
     * @param updateType Update type
     */
    fun markPopupAsShown(version: String, updateType: Int) {
        actualStorage?.setLastPopupShownTime(System.currentTimeMillis(), version)
        actualStorage?.setLastPopupVersion(version)
    }
    
    /**
     * Decrement skip attempts (for type 3)
     * @param version Version identifier
     * @return New remaining count
     */
    fun skipUpdate(version: String): Int {
        return actualStorage?.decrementSkipAttempts(version) ?: 0
    }
    
    /**
     * Initialize skip attempts from API (for type 3)
     * @param skipAttempts Skip attempts from API
     * @param version Version identifier
     */
    fun initializeSkipAttempts(skipAttempts: Int, version: String) {
        actualStorage?.initializeSkipAttempts(skipAttempts, version)
    }
    
    private fun shouldShowBadgeForType0(
        channelData: ChannelData,
        isUpdateAvailable: Boolean
    ): Boolean {
        if (channelData.updateType != 0 || !isUpdateAvailable) return false
        
        val postURL = channelData.postUrl ?: return false
        return !(actualStorage?.isPostOpened(postURL) ?: false)
    }
    
    private fun shouldShowPopupForTypes2and3(
        channelData: ChannelData,
        isUpdateAvailable: Boolean
    ): Boolean {
        if (channelData.updateType != 2 && channelData.updateType != 3) return false
        if (!isUpdateAvailable) return false
        
        val version = channelData.appVersionCode
        
        if (channelData.updateType == 2) {
            return actualStorage?.shouldShowPopup(channelData.showInterval, version) ?: true
        }
        
        if (channelData.updateType == 3) {
            val remaining = actualStorage?.getRemainingSkipAttempts(version) ?: 0
            if (remaining == 0) {
                actualStorage?.initializeSkipAttempts(channelData.skipAttempts, version)
            }
            return true
        }
        
        return false
    }
    
    private fun shouldShowUpdateButtonForType1(
        channelData: ChannelData,
        isUpdateAvailable: Boolean
    ): Boolean {
        return channelData.updateType == 1 && isUpdateAvailable
    }
    
    private fun getRemainingSkipAttempts(channelData: ChannelData): Int {
        if (channelData.updateType != 3) return 0
        return actualStorage?.getRemainingSkipAttempts(channelData.appVersionCode) ?: 0
    }
    
    private fun getBadgeURL(channelData: ChannelData): String? {
        if (channelData.updateType != 0) return null
        
        val postURL = channelData.postUrl ?: return channelData.postsUrl
        return if (actualStorage?.isPostOpened(postURL) == true) {
            channelData.postsUrl
        } else {
            postURL
        }
    }
}
