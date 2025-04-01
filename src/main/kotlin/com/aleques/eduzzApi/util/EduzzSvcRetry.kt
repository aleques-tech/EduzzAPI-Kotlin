package com.aleques.eduzzApi.util

import io.vertx.core.VertxException
import kotlinx.coroutines.delay

internal suspend fun <T> eduzzSvcRetry(delayAmount: Long = 1200, service: suspend () -> T): T {
    while (true) {
        try {
            return service()
        } catch (e: VertxException) {
            when {
                e.message?.contains("429") == true ||
                e.message?.contains("500") == true ||
                e.message?.contains("502") == true ||
                e.message?.contains("503") == true ||
                e.message?.contains("504") == true -> {
                    delay(delayAmount)
                    continue
                }
                else -> throw e
            }
        }
    }
}
