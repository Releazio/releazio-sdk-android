package com.releazio.sdk.core

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Configuration object for Releazio SDK
 */
data class ReleazioConfiguration(
    /**
     * API key for authentication with Releazio API
     */
    val apiKey: String,

    /**
     * Enable debug logging
     */
    val debugLoggingEnabled: Boolean = false,

    /**
     * Timeout for network requests in milliseconds (default: 30 seconds)
     */
    val networkTimeout: Long = 30_000L,

    /**
     * Enable analytics tracking
     */
    val analyticsEnabled: Boolean = true,

    /**
     * Cache expiration time in seconds (default: 1 hour)
     */
    val cacheExpirationTime: Long = 3600L,

    /**
     * Locale for SDK localization (default: "en")
     * Supported locales: "en", "ru"
     */
    val locale: String = "en",

    /**
     * Badge color for update indicator (optional, default: system yellow)
     */
    @ColorInt
    val badgeColor: Int? = null
) {

    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(networkTimeout > 0) { "Network timeout must be positive" }
        require(cacheExpirationTime > 0) { "Cache expiration time must be positive" }
        require(locale in listOf("en", "ru")) { "Unsupported locale: $locale. Supported: en, ru" }
    }

    /**
     * Validate configuration parameters
     * @return true if configuration is valid
     */
    fun validate(): Boolean {
        return apiKey.isNotBlank() &&
                apiKey.length >= 8 && // Basic validation for API key format
                networkTimeout > 0 &&
                cacheExpirationTime > 0 &&
                locale in listOf("en", "ru")
    }

    companion object {
        /**
         * Default badge color (system yellow)
         */
        @ColorInt
        val DEFAULT_BADGE_COLOR = Color.parseColor("#FFD700")
    }
}
