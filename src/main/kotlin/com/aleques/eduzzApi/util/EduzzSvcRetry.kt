package com.aleques.eduzzApi.util

import io.vertx.core.VertxException
import kotlinx.coroutines.delay
import kotlin.random.Random

internal class RateLimitTracker {
    private var remainingRequests = 30 // Default to max limit
    private var lastRequestTime = System.currentTimeMillis()
    private val windowSize = 60_000L // 60 seconds window

    fun updateFromHeaders(headers: Map<String, String>) {
        headers["x-ratelimit-remaining"]?.toIntOrNull()?.let {
            remainingRequests = it
            lastRequestTime = System.currentTimeMillis()
        }
    }

    suspend fun ensureRateLimit() {
        if (remainingRequests <= 1) {
            val timeSinceLast = System.currentTimeMillis() - lastRequestTime
            val waitTime = windowSize - timeSinceLast
            if (waitTime > 0) {
                delay(waitTime)
            }
            remainingRequests = 30 // Reset after waiting
        }
    }
}

internal suspend fun <T> eduzzSvcRetry(
    maxAttempts: Int = 5,
    baseDelay: Long = 5000, // 5 seconds base delay
    service: suspend (RateLimitTracker) -> T
): T {
    val rateLimitTracker = RateLimitTracker()
    var attempt = 0
    
    while (true) {
        try {
            rateLimitTracker.ensureRateLimit()
            return service(rateLimitTracker)
        } catch (e: VertxException) {
            attempt++
            when {
                e.message?.contains("429") == true -> {
                    if (attempt >= maxAttempts) throw e
                    val jitter = Random.nextLong(0, 1000)
                    val delayTime = (baseDelay * (1L shl (attempt - 1))) + jitter
                    delay(minOf(delayTime, 60_000)) // Cap at 60 seconds
                }
                e.message?.contains("50[0234]") == true -> {
                    if (attempt >= maxAttempts) throw e
                    delay(1000L * attempt) // Linear backoff for server errors
                }
                else -> throw e
            }
        }
    }
}
