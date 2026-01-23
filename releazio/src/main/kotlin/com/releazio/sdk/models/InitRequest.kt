package com.releazio.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for device initialization endpoint
 */
@Serializable
data class InitRequest(
    /**
     * Channel identifier (e.g., "apk", "googleplay", "galaxystore")
     */
    val channel: String? = null,

    /**
     * Application package name
     */
    @SerialName("app_id")
    val appId: String? = null,

    /**
     * Application version code
     */
    @SerialName("app_version_code")
    val appVersionCode: String? = null,

    /**
     * Application version name
     */
    @SerialName("app_version_name")
    val appVersionName: String? = null,

    /**
     * Operating system type (e.g., "android")
     */
    @SerialName("os_type")
    val osType: String? = null,

    /**
     * Region code (ISO 3166-1 alpha-2)
     */
    val region: String? = null,

    /**
     * Comma-separated list of installed app store package names
     */
    @SerialName("market_packages")
    val marketPackages: String? = null,

    /**
     * Locale language code (ISO 639-1)
     */
    val locale: String? = null,

    /**
     * OS version code (e.g., "34" for Android 14)
     */
    @SerialName("os_version_code")
    val osVersionCode: String? = null,

    /**
     * Device manufacturer
     */
    @SerialName("device_manufacturer")
    val deviceManufacturer: String? = null,

    /**
     * Device brand
     */
    @SerialName("device_brand")
    val deviceBrand: String? = null,

    /**
     * Device model
     */
    @SerialName("device_model")
    val deviceModel: String? = null,

    /**
     * SDK version
     */
    @SerialName("sdk_version")
    val sdkVersion: String? = null,

    /**
     * OS API level
     */
    @SerialName("os_api_level")
    val osApiLevel: String? = null,

    /**
     * Timezone identifier (e.g., "Europe/Moscow")
     */
    val timezone: String? = null,

    /**
     * Device unique identifier
     */
    @SerialName("device_id")
    val deviceId: String? = null,

    /**
     * Screen width in pixels
     */
    @SerialName("screen_width")
    val screenWidth: Int? = null,

    /**
     * Screen height in pixels
     */
    @SerialName("screen_height")
    val screenHeight: Int? = null,

    /**
     * Screen scale (density DPI)
     */
    @SerialName("screen_scale")
    val screenScale: Int? = null,

    /**
     * Whether device is an emulator
     */
    @SerialName("is_emulator")
    val isEmulator: Boolean? = null
)
