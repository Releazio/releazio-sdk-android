package com.releazio.sdk.services

import com.releazio.sdk.core.ReleazioConfiguration

/**
 * Simple cache service for SDK data
 */
class CacheService(
    private val configuration: ReleazioConfiguration
) {
    
    private val cache = mutableMapOf<String, CacheEntry>()
    
    /**
     * Cache entry with expiration
     */
    private data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttl: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > ttl
        }
    }
    
    /**
     * Get cached data
     * @param key Cache key
     * @return Cached data or null if not found or expired
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = cache[key] ?: return null
        
        return if (entry.isExpired()) {
            cache.remove(key)
            null
        } else {
            entry.data as T
        }
    }
    
    /**
     * Put data in cache
     * @param key Cache key
     * @param data Data to cache
     * @param ttl Time to live in milliseconds (default: from configuration)
     */
    fun put(key: String, data: Any, ttl: Long = configuration.cacheExpirationTime * 1000) {
        cache[key] = CacheEntry(
            data = data,
            timestamp = System.currentTimeMillis(),
            ttl = ttl
        )
    }
    
    /**
     * Remove data from cache
     * @param key Cache key
     */
    fun remove(key: String) {
        cache.remove(key)
    }
    
    /**
     * Clear all cached data
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * Clear expired entries
     */
    fun clearExpired() {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.isExpired()) {
                iterator.remove()
            }
        }
    }
}
