package com.aleques.eduzzApi.util

import kotlinx.coroutines.delay
import retrofit2.HttpException

internal suspend fun <T> eduzzSvcRetry(delayAmount: Long = 1200, service: suspend () -> T): T {
    while (true) {
        try {
            return service()
        } catch (e: HttpException) {
            val code = e.code()
            when (code) {
                429,500, in 502..504 -> {
                    delay(delayAmount)
                    continue
                }
            }
            throw e
        }
    }
}