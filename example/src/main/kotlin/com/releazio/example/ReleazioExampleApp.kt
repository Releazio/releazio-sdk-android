package com.releazio.example

import android.app.Application
import android.util.Log
import com.releazio.sdk.Releazio
import com.releazio.sdk.core.ReleazioConfiguration

/**
 * Application class for Releazio Example
 * Configures the Releazio SDK on app startup
 */
class ReleazioExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureReleazioSDK()
    }

    /**
     * Configure Releazio SDK with API key and settings
     */
    private fun configureReleazioSDK() {
        val apiKey = "ngqOJgnrvwFqSvVyqUOhxCvnRHisKkoiuI/PycYjiGc="

        // Create configuration
        val configuration = ReleazioConfiguration(
            apiKey = apiKey,
            debugLoggingEnabled = true,
            networkTimeout = 30_000L,
            analyticsEnabled = true,
            cacheExpirationTime = 3600L,
            locale = "ru"
        )

        // Configure SDK
        Releazio.configure(configuration, this)

        Log.d("ReleazioExample", "✅ Releazio SDK configured successfully")
        Log.d("ReleazioExample", "🔑 API Key: ${apiKey.take(10)}...")
    }
}


