package com.releazio.sdk.core

/**
 * Custom error types for Releazio SDK
 */
sealed class ReleazioError : Exception() {

    /**
     * SDK is not configured
     */
    object ConfigurationMissing : ReleazioError() {
        override val message: String
            get() = "Releazio SDK is not configured. Call Releazio.configure() first."
    }

    /**
     * Invalid API key
     */
    object InvalidApiKey : ReleazioError() {
        override val message: String
            get() = "Invalid API key provided."
    }

    /**
     * Invalid configuration parameters
     */
    data class InvalidConfiguration(val reason: String) : ReleazioError() {
        override val message: String
            get() = "Invalid configuration: $reason"
    }


    /**
     * Network request failed
     */
    data class NetworkError(override val cause: Throwable) : ReleazioError() {
        override val message: String
            get() = "Network error: ${cause.message}"
    }

    /**
     * Invalid URL
     */
    data class InvalidURL(val url: String) : ReleazioError() {
        override val message: String
            get() = "Invalid URL: $url"
    }

    /**
     * Request timeout
     */
    object RequestTimeout : ReleazioError() {
        override val message: String
            get() = "Request timed out."
    }

    /**
     * No internet connection
     */
    object NoInternetConnection : ReleazioError() {
        override val message: String
            get() = "No internet connection available."
    }

    /**
     * Request was cancelled
     */
    object Cancelled : ReleazioError() {
        override val message: String
            get() = "Request was cancelled."
    }

    /**
     * Server error with status code
     */
    data class ServerError(val statusCode: Int, val errorMessage: String?) : ReleazioError() {
        override val message: String
            get() = if (errorMessage != null) {
                "Server error $statusCode: $errorMessage"
            } else {
                "Server error with status code: $statusCode"
            }
    }

    /**
     * Rate limit exceeded
     */
    data class RateLimitExceeded(val retryAfter: Long?) : ReleazioError() {
        override val message: String
            get() = if (retryAfter != null) {
                "Rate limit exceeded. Retry after $retryAfter seconds."
            } else {
                "Rate limit exceeded."
            }
    }


    /**
     * Invalid API response
     */
    object InvalidResponse : ReleazioError() {
        override val message: String
            get() = "Invalid API response received."
    }

    /**
     * API returned error
     */
    data class ApiError(val code: String, val errorMessage: String?) : ReleazioError() {
        override val message: String
            get() = if (errorMessage != null) {
                "API error ($code): $errorMessage"
            } else {
                "API error with code: $code"
            }
    }

    /**
     * Missing required data in response
     */
    data class MissingData(val data: String) : ReleazioError() {
        override val message: String
            get() = "Missing required data in response: $data"
    }

    /**
     * Failed to decode JSON response
     */
    data class DecodingError(override val cause: Throwable) : ReleazioError() {
        override val message: String
            get() = "Failed to decode response: ${cause.message}"
    }


    /**
     * Cache operation failed
     */
    data class CacheError(override val cause: Throwable) : ReleazioError() {
        override val message: String
            get() = "Cache error: ${cause.message}"
    }

    /**
     * Data not found in cache
     */
    object CacheMiss : ReleazioError() {
        override val message: String
            get() = "Requested data not found in cache."
    }


    /**
     * Failed to present UI
     */
    object UIPresentationError : ReleazioError() {
        override val message: String
            get() = "Failed to present UI component."
    }

    /**
     * Missing context for UI operations
     */
    object MissingUIContext : ReleazioError() {
        override val message: String
            get() = "No context available for UI operations."
    }


    /**
     * Invalid version format
     */
    data class InvalidVersionFormat(val version: String) : ReleazioError() {
        override val message: String
            get() = "Invalid version format: $version"
    }

    /**
     * Version comparison failed
     */
    object VersionComparisonError : ReleazioError() {
        override val message: String
            get() = "Failed to compare app versions."
    }
}

/**
 * Extension function to convert any error to ReleazioError
 */
fun Throwable.asReleazioError(): ReleazioError {
    return when (this) {
        is ReleazioError -> this
        is java.net.UnknownHostException -> ReleazioError.NoInternetConnection
        is java.net.SocketTimeoutException -> ReleazioError.RequestTimeout
        is java.net.ConnectException -> ReleazioError.NoInternetConnection
        is kotlinx.coroutines.CancellationException -> ReleazioError.Cancelled
        else -> ReleazioError.NetworkError(this)
    }
}
