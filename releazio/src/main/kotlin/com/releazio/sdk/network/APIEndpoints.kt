package com.releazio.sdk.network

import java.net.URL

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
     * @param locale Optional locale override
     * @param channel Optional channel override
     * @return Endpoint URL with query parameters if provided
     */
    fun getConfig(locale: String? = null, channel: String? = null): URL {
        if (locale == null && channel == null) {
            return baseURL
        }
        
        val queryParams = mutableListOf<String>()
        locale?.let { queryParams.add("locale=$it") }
        channel?.let { queryParams.add("channel=$it") }
        
        return if (queryParams.isNotEmpty()) {
            URL("${baseURL}?${queryParams.joinToString("&")}")
        } else {
            baseURL
        }
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
