package com.releazio.sdk.network

import java.net.URL
import java.net.URLEncoder

/**
 * API endpoints for Releazio service
 */
object APIEndpoints {

    /**
     * API base URL
     */
    val baseURL = URL("https://check.releazio.com")

    /**
     * Get application configuration and releases
     * @param channel Channel identifier (e.g., "playstore", "galaxystore")
     * @param appId Application package name
     * @param appVersionCode Application version code
     * @param appVersionName Application version name
     * @param phoneLocaleCountry Phone locale country code
     * @param phoneLocaleLanguage Phone locale language code
     * @param osVersionCode OS API level
     * @param deviceManufacturer Device manufacturer
     * @param deviceBrand Device brand
     * @param deviceModel Device model
     * @return Endpoint URL with query parameters
     */
    fun getConfig(
        channel: String,
        appId: String?,
        appVersionCode: String?,
        appVersionName: String?,
        phoneLocaleCountry: String?,
        phoneLocaleLanguage: String?,
        osVersionCode: Int?,
        deviceManufacturer: String,
        deviceBrand: String,
        deviceModel: String?
    ): URL {
        val queryParams = mutableListOf<String>()
        
        // Required parameter
        queryParams.add("channel=${URLEncoder.encode(channel, "UTF-8")}")
        
        // Optional parameters
        appId?.let { queryParams.add("app_id=${URLEncoder.encode(it, "UTF-8")}") }
        appVersionCode?.let { queryParams.add("app_version_code=${URLEncoder.encode(it, "UTF-8")}") }
        appVersionName?.let { queryParams.add("app_version_name=${URLEncoder.encode(it, "UTF-8")}") }
        phoneLocaleCountry?.let { queryParams.add("phone_locale_country=${URLEncoder.encode(it, "UTF-8")}") }
        phoneLocaleLanguage?.let { queryParams.add("phone_locale_language=${URLEncoder.encode(it, "UTF-8")}") }
        osVersionCode?.let { queryParams.add("os_version_code=$it") }
        queryParams.add("device_manufacturer=${URLEncoder.encode(deviceManufacturer, "UTF-8")}")
        queryParams.add("device_brand=${URLEncoder.encode(deviceBrand, "UTF-8")}")
        deviceModel?.let { queryParams.add("device_model=${URLEncoder.encode(it, "UTF-8")}") }
        
        return URL("${baseURL}?${queryParams.joinToString("&")}")
    }

    /**
     * Device initialization endpoint
     * @return Endpoint URL for device init
     */
    fun init(): URL {
        return URL("${baseURL}/init")
    }
}

/**
 * HTTP methods
 */
enum class HTTPMethod(val value: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH")
}

/**
 * Request builder
 */
data class APIRequest(
    val url: URL,
    val method: HTTPMethod = HTTPMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray? = null,
    val timeout: Long = 30_000L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as APIRequest

        if (url != other.url) return false
        if (method != other.method) return false
        if (headers != other.headers) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false
        if (timeout != other.timeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + timeout.hashCode()
        return result
    }
}

/**
 * APIRequest extensions
 */
object APIRequestBuilder {

    /**
     * Create GET request
     * @param url URL
     * @param headers Additional headers
     * @param timeout Request timeout
     * @return APIRequest
     */
    fun get(
        url: URL,
        headers: Map<String, String> = emptyMap(),
        timeout: Long = 30_000L
    ): APIRequest {
        return APIRequest(
            url = url,
            method = HTTPMethod.GET,
            headers = headers,
            timeout = timeout
        )
    }

    /**
     * Create POST request
     * @param url URL
     * @param headers Additional headers
     * @param body Request body
     * @param timeout Request timeout
     * @return APIRequest
     */
    fun post(
        url: URL,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
        timeout: Long = 30_000L
    ): APIRequest {
        return APIRequest(
            url = url,
            method = HTTPMethod.POST,
            headers = headers,
            body = body,
            timeout = timeout
        )
    }
}
