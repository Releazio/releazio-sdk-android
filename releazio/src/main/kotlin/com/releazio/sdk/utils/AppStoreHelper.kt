package com.releazio.sdk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Helper for detecting and opening app stores
 */
object AppStoreHelper {

    /**
     * App store package identifiers
     */
    enum class AppStore(val packageName: String) {
        PLAY_STORE("com.android.vending"),
        GALAXY_STORE("com.sec.android.app.samsungapps"),
        APP_GALLERY("com.huawei.appmarket"),
        RUSTORE("ru.rustore.app"),
        NASHSTORE("com.yandex.store"),
        RUMARKET("com.ru_market.app"),
        GETAPPS("com.mi.globalappstore")
    }

    /**
     * Check if a specific app store is installed
     * @param context Android context
     * @param store App store to check
     * @return True if store is installed
     */
    fun isStoreInstalled(context: Context, store: AppStore): Boolean {
        return try {
            context.packageManager.getPackageInfo(store.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Detect installed app stores on device
     * @param context Android context
     * @return List of installed app stores
     */
    fun detectInstalledStores(context: Context): List<AppStore> {
        return AppStore.values().filter { isStoreInstalled(context, it) }
    }

    /**
     * Detect primary app store (prefer Play Store, then others)
     * @param context Android context
     * @return Primary app store or null if none installed
     */
    fun detectPrimaryStore(context: Context): AppStore? {
        val installed = detectInstalledStores(context)
        return installed.firstOrNull { it == AppStore.PLAY_STORE } ?: installed.firstOrNull()
    }

    /**
     * Try to open URL with specific app store
     * @param context Android context
     * @param urlString URL to open
     * @param store App store package name (optional)
     * @return True if opened successfully
     */
    fun openURLWithStore(context: Context, urlString: String, store: AppStore? = null): Boolean {
        return try {
            val uri = Uri.parse(urlString)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                // If specific store is provided and installed, use it
                store?.let {
                    if (isStoreInstalled(context, it)) {
                        setPackage(it.packageName)
                    }
                }
            }
            
            // Check if intent can be resolved
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Fallback: try without package restriction
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Detect app store from URL
     * @param urlString URL to analyze
     * @return Detected app store or null
     */
    fun detectStoreFromURL(urlString: String): AppStore? {
        return when {
            urlString.contains("play.google.com") || urlString.contains("market.android.com") -> AppStore.PLAY_STORE
            urlString.contains("galaxyapps") || urlString.contains("samsungapps") -> AppStore.GALAXY_STORE
            urlString.contains("appgallery") || urlString.contains("huawei") -> AppStore.APP_GALLERY
            urlString.contains("rustore") -> AppStore.RUSTORE
            urlString.contains("yandex.store") || urlString.contains("nashstore") -> AppStore.NASHSTORE
            urlString.contains("ru_market") || urlString.contains("rumarket") -> AppStore.RUMARKET
            urlString.contains("getapps") || urlString.contains("xiaomi") -> AppStore.GETAPPS
            else -> null
        }
    }
}

