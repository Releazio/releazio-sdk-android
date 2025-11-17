package com.releazio.sdk.network

import com.releazio.sdk.core.ReleazioConfiguration
import com.releazio.sdk.core.ReleazioError
import com.releazio.sdk.core.asReleazioError
import com.releazio.sdk.models.PaginationInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Protocol for network client implementation
 */
interface NetworkClientProtocol {
    suspend fun requestRaw(request: APIRequest): String
}

/**
 * Network client for making API requests
 */
class NetworkClient(
    private val configuration: ReleazioConfiguration
) : NetworkClientProtocol {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Ktor HTTP client
     */
    private val client = HttpClient(Android) {
        // Content negotiation for JSON
        install(ContentNegotiation) {
            json(json)
        }

        // Timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = configuration.networkTimeout
            connectTimeoutMillis = configuration.networkTimeout
            socketTimeoutMillis = configuration.networkTimeout
        }

        // Logging (only in debug mode)
        if (configuration.debugLoggingEnabled) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("ReleazioNetwork", message)
                    }
                }
            }
        }

        // Default request configuration
        install(DefaultRequest) {
            header("Accept", "application/json")
            header("Authorization", configuration.apiKey)
        }

        // Response validation - handled manually in request methods
    }

    /**
     * Make a request and return raw JSON string
     * @param request API request configuration
     * @return Raw JSON string
     * @throws ReleazioError
     */
    override suspend fun requestRaw(request: APIRequest): String {
        return try {
            val httpResponse = client.request(request.url) {
                method = HttpMethod.parse(request.method.value)
                timeout {
                    requestTimeoutMillis = request.timeout
                }
                
                // Add custom headers
                request.headers.forEach { (key, value) ->
                    header(key, value)
                }
                
                // Add body if present
                request.body?.let { body ->
                    setBody(body)
                    contentType(ContentType.Application.Json)
                }
            }
            
            // Validate response status
            when (httpResponse.status.value) {
                in 200..299 -> {
                    // Success - return body as string
                    httpResponse.body<String>()
                }
                401 -> throw ReleazioError.InvalidApiKey
                403 -> throw ReleazioError.ApiError("FORBIDDEN", "Access forbidden")
                404 -> throw ReleazioError.ApiError("NOT_FOUND", "Resource not found")
                429 -> {
                    val retryAfter = httpResponse.headers["Retry-After"]?.toLongOrNull()
                    throw ReleazioError.RateLimitExceeded(retryAfter)
                }
                in 500..599 -> {
                    val errorMessage = try {
                        httpResponse.body<String>()
                    } catch (e: Exception) {
                        null
                    }
                    throw ReleazioError.ServerError(httpResponse.status.value, errorMessage)
                }
                else -> {
                    val errorMessage = try {
                        httpResponse.body<String>()
                    } catch (e: Exception) {
                        null
                    }
                    throw ReleazioError.ApiError("HTTP_ERROR", errorMessage)
                }
            }
        } catch (e: ReleazioError) {
            throw e
        } catch (e: Exception) {
            throw e.asReleazioError()
        }
    }
    
    /**
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}

/**
 * Extension function to make typed request
 */
inline suspend fun <reified T> NetworkClientProtocol.request(request: APIRequest): T {
    val raw = requestRaw(request)
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    return json.decodeFromString(raw)
}

/**
 * Extension function to make paginated request
 */
inline suspend fun <reified T> NetworkClientProtocol.requestWithPagination(request: APIRequest): Pair<List<T>, PaginationInfo?> {
    val response: com.releazio.sdk.models.APIResponse<List<T>> = request<com.releazio.sdk.models.APIResponse<List<T>>>(request)
    return Pair(response.data ?: emptyList(), response.meta?.pagination)
}
