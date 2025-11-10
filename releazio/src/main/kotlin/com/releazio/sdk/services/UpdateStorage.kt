package com.releazio.sdk.services

import android.content.Context
import android.content.SharedPreferences

/**
 * Service for storing update-related data locally
 */
class UpdateStorage(
    private val context: Context,
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "releazio_prefs",
        Context.MODE_PRIVATE
    )
) {

    companion object {
        private const val KEY_SKIP_ATTEMPTS_REMAINING = "releazio_skip_attempts_remaining"
        private const val KEY_LAST_POPUP_SHOWN_TIME = "releazio_last_popup_shown_time"
        private const val KEY_LAST_POPUP_VERSION = "releazio_last_popup_version"
        private const val KEY_POST_OPENED_IDS = "releazio_post_opened_ids"
        private const val KEY_CURRENT_VERSION = "releazio_current_version"
    }

    /**
     * Get remaining skip attempts for current version
     * @param version Version identifier
     * @return Remaining skip attempts
     */
    fun getRemainingSkipAttempts(version: String): Int {
        val key = "${KEY_SKIP_ATTEMPTS_REMAINING}_$version"
        return prefs.getInt(key, 0)
    }

    /**
     * Set remaining skip attempts for current version
     * @param attempts Number of remaining attempts
     * @param version Version identifier
     */
    fun setRemainingSkipAttempts(attempts: Int, version: String) {
        val key = "${KEY_SKIP_ATTEMPTS_REMAINING}_$version"
        prefs.edit().putInt(key, attempts).apply()
    }

    /**
     * Decrement skip attempts for current version
     * @param version Version identifier
     * @return New remaining count
     */
    fun decrementSkipAttempts(version: String): Int {
        val current = getRemainingSkipAttempts(version)
        val newValue = maxOf(0, current - 1)
        setRemainingSkipAttempts(newValue, version)
        return newValue
    }

    /**
     * Initialize skip attempts from API value
     * @param skipAttempts Skip attempts from API
     * @param version Version identifier
     */
    fun initializeSkipAttempts(skipAttempts: Int, version: String) {
        // Only initialize if not already set for this version
        val key = "${KEY_SKIP_ATTEMPTS_REMAINING}_$version"
        if (!prefs.contains(key)) {
            setRemainingSkipAttempts(skipAttempts, version)
        }
    }


    /**
     * Get last popup shown time
     * @param version Version identifier
     * @return Date of last popup shown or null
     */
    fun getLastPopupShownTime(version: String): Long? {
        val key = "${KEY_LAST_POPUP_SHOWN_TIME}_$version"
        val timestamp = prefs.getLong(key, 0L)
        return if (timestamp > 0) timestamp else null
    }

    /**
     * Set last popup shown time
     * @param timestamp Timestamp of popup shown
     * @param version Version identifier
     */
    fun setLastPopupShownTime(timestamp: Long, version: String) {
        val key = "${KEY_LAST_POPUP_SHOWN_TIME}_$version"
        prefs.edit().putLong(key, timestamp).apply()
    }

    /**
     * Check if enough time has passed since last popup shown
     * @param interval Show interval in minutes
     * @param version Version identifier
     * @return True if enough time has passed or never shown
     */
    fun shouldShowPopup(interval: Int, version: String): Boolean {
        if (interval <= 0) {
            // If interval is 0, show every time
            return true
        }
        
        val lastShown = getLastPopupShownTime(version) ?: return true // Never shown before
        
        val minutesSinceLastShow = (System.currentTimeMillis() - lastShown) / (1000 * 60)
        return minutesSinceLastShow >= interval
    }

    /**
     * Get last popup version
     * @return Version string or null
     */
    fun getLastPopupVersion(): String? {
        return prefs.getString(KEY_LAST_POPUP_VERSION, null)
    }

    /**
     * Set last popup version
     * @param version Version string
     */
    fun setLastPopupVersion(version: String) {
        prefs.edit().putString(KEY_LAST_POPUP_VERSION, version).apply()
    }


    /**
     * Check if post was opened
     * @param postId Post identifier (typically postUrl or version)
     * @return True if post was opened
     */
    fun isPostOpened(postId: String): Boolean {
        val openedIds = getOpenedPostIds()
        return openedIds.contains(postId)
    }

    /**
     * Mark post as opened
     * @param postId Post identifier
     */
    fun markPostAsOpened(postId: String) {
        val openedIds = getOpenedPostIds().toMutableSet()
        if (openedIds.add(postId)) {
            setOpenedPostIds(openedIds.toList())
        }
    }

    /**
     * Get all opened post IDs
     * @return Set of opened post IDs
     */
    fun getOpenedPostIds(): Set<String> {
        val ids = prefs.getStringSet(KEY_POST_OPENED_IDS, emptySet())
        return ids ?: emptySet()
    }

    /**
     * Set opened post IDs
     * @param ids Set of post IDs
     */
    private fun setOpenedPostIds(ids: List<String>) {
        prefs.edit().putStringSet(KEY_POST_OPENED_IDS, ids.toSet()).apply()
    }


    /**
     * Get stored current version
     * @return Version string or null
     */
    fun getCurrentVersion(): String? {
        return prefs.getString(KEY_CURRENT_VERSION, null)
    }

    /**
     * Set current version
     * @param version Version string
     */
    fun setCurrentVersion(version: String) {
        prefs.edit().putString(KEY_CURRENT_VERSION, version).apply()
    }

    /**
     * Check if version has changed since last check
     * @param version Current version to check
     * @return True if version changed or never set
     */
    fun hasVersionChanged(version: String): Boolean {
        val storedVersion = getCurrentVersion()
        return storedVersion != version
    }


    /**
     * Clear all update-related data
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Clear data for specific version
     * @param version Version to clear data for
     */
    fun clearData(version: String) {
        val skipKey = "${KEY_SKIP_ATTEMPTS_REMAINING}_$version"
        val timeKey = "${KEY_LAST_POPUP_SHOWN_TIME}_$version"
        prefs.edit()
            .remove(skipKey)
            .remove(timeKey)
            .apply()
    }
}
