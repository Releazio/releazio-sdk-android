package com.releazio.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper
 */
@Serializable
data class APIResponse<T>(
    /**
     * Response data
     */
    val data: T? = null,

    /**
     * Success status
     */
    val success: Boolean = true,

    /**
     * Response message
     */
    val message: String? = null,

    /**
     * Error information
     */
    val error: APIError? = null,

    /**
     * Metadata (pagination, etc.)
     */
    val meta: APIMetadata? = null,

    /**
     * Response timestamp
     */
    val timestamp: Long = System.currentTimeMillis(),

    /**
     * Request ID for tracking
     */
    @SerialName("request_id")
    val requestId: String? = null
) {
    /**
     * Check if response is successful
     */
    val isSuccess: Boolean
        get() = success && error == null

    /**
     * Get error or null if successful
     */
    val responseError: com.releazio.sdk.core.ReleazioError?
        get() = error?.let { com.releazio.sdk.core.ReleazioError.ApiError(it.code, it.message) }

    /**
     * Extract data or throw error
     * @return Response data
     * @throws ReleazioError if response is unsuccessful
     */
    fun unwrap(): T {
        if (data == null) {
            throw responseError ?: com.releazio.sdk.core.ReleazioError.InvalidResponse
        }
        if (!isSuccess) {
            throw responseError ?: com.releazio.sdk.core.ReleazioError.InvalidResponse
        }
        return data
    }

    /**
     * Extract data with default value
     * @param defaultValue Default value if data is null
     * @return Response data or default value
     */
    fun dataOr(defaultValue: T): T = data ?: defaultValue
}

/**
 * API error information
 */
@Serializable
data class APIError(
    /**
     * Error code
     */
    val code: String,

    /**
     * Error message
     */
    val message: String,

    /**
     * Error details
     */
    val details: List<String>? = null,

    /**
     * Error type
     */
    val type: ErrorType? = null,

    /**
     * Field name (for validation errors)
     */
    val field: String? = null
) {
    /**
     * Error types
     */
    @Serializable
    enum class ErrorType(val value: String) {
        VALIDATION("validation"),
        AUTHENTICATION("authentication"),
        AUTHORIZATION("authorization"),
        NOT_FOUND("not_found"),
        RATE_LIMIT("rate_limit"),
        SERVER("server"),
        NETWORK("network"),
        UNKNOWN("unknown");

        /**
         * Display name
         */
        val displayName: String
            get() = when (this) {
                VALIDATION -> "Validation Error"
                AUTHENTICATION -> "Authentication Error"
                AUTHORIZATION -> "Authorization Error"
                NOT_FOUND -> "Not Found"
                RATE_LIMIT -> "Rate Limit Exceeded"
                SERVER -> "Server Error"
                NETWORK -> "Network Error"
                UNKNOWN -> "Unknown Error"
            }
    }
}

/**
 * API metadata (pagination, etc.)
 */
@Serializable
data class APIMetadata(
    /**
     * Current page number
     */
    val page: Int? = null,

    /**
     * Number of items per page
     */
    @SerialName("per_page")
    val perPage: Int? = null,

    /**
     * Total number of items
     */
    val total: Int? = null,

    /**
     * Total number of pages
     */
    @SerialName("total_pages")
    val totalPages: Int? = null,

    /**
     * Has next page
     */
    @SerialName("has_next_page")
    val hasNextPage: Boolean? = null,

    /**
     * Has previous page
     */
    @SerialName("has_previous_page")
    val hasPreviousPage: Boolean? = null,

    /**
     * API version
     */
    @SerialName("api_version")
    val apiVersion: String? = null,

    /**
     * Server timestamp
     */
    @SerialName("server_time")
    val serverTime: Long? = null,

    /**
     * Rate limit information
     */
    @SerialName("rate_limit")
    val rateLimit: RateLimitInfo? = null
) {
    /**
     * Pagination information
     */
    val pagination: PaginationInfo?
        get() = if (page != null && perPage != null && total != null) {
            PaginationInfo(
                page = page,
                perPage = perPage,
                total = total,
                totalPages = totalPages ?: ((total + perPage - 1) / perPage),
                hasNextPage = hasNextPage ?: (page * perPage < total),
                hasPreviousPage = hasPreviousPage ?: (page > 1)
            )
        } else null
}

/**
 * Pagination information
 */
data class PaginationInfo(
    val page: Int,
    val perPage: Int,
    val total: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
) {
    /**
     * Start index (1-based)
     */
    val startIndex: Int
        get() = ((page - 1) * perPage) + 1

    /**
     * End index (1-based)
     */
    val endIndex: Int
        get() = minOf(page * perPage, total)

    /**
     * Items remaining
     */
    val remainingItems: Int
        get() = maxOf(0, total - endIndex)
}

/**
 * Rate limit information
 */
@Serializable
data class RateLimitInfo(
    /**
     * Maximum requests per window
     */
    val limit: Int,

    /**
     * Remaining requests in current window
     */
    val remaining: Int,

    /**
     * When the rate limit window resets (timestamp)
     */
    @SerialName("reset_time")
    val resetTime: Long? = null,

    /**
     * Seconds until reset
     */
    @SerialName("reset_in")
    val resetIn: Int? = null
) {
    /**
     * Is rate limit exceeded
     */
    val isExceeded: Boolean
        get() = remaining <= 0

    /**
     * Usage percentage (0-1)
     */
    val usagePercentage: Double
        get() = if (limit > 0) {
            (limit - remaining).toDouble() / limit.toDouble()
        } else 0.0
}

/**
 * Response wrapper for array data with pagination
 */
typealias ArrayAPIResponse<T> = APIResponse<List<T>>
