package com.aleques.eduzzApi.util

import kotlinx.coroutines.delay
import retrofit2.HttpException

internal suspend fun <T> eduzzSvcRetry(service: suspend () -> T): T {
    while (true) {
        try {
            return service()
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 500 || code == 429) {
                delay(600)
                continue
            }
            throw e
        }
    }
}