package com.aleques.eduzzApi.util

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import com.aleques.eduzzApi.EduzzAuthResponse
import com.aleques.eduzzApi.EduzzGetUserResponse
import com.aleques.eduzzApi.EduzzGetInvoiceResponse
import com.aleques.eduzzApi.EduzzGetTaxDocResponse
import com.aleques.eduzzApi.EduzzLastDaysAmountResponse
import com.aleques.eduzzApi.EduzzFinancialStatementResponse
import com.aleques.eduzzApi.ValidationException
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Duration

internal val vertx = Vertx.vertx()


internal val vertxHttpClient = WebClient.create(vertx, WebClientOptions()
    .setConnectTimeout(Duration.ofSeconds(50).toMillis().toInt())
    .setIdleTimeout(Duration.ofSeconds(120).toMillis().toInt())
    .setMaxPoolSize(20)
)

internal suspend inline fun <reified T> vertxRequest(
    method: HttpMethod,
    url: String,
    headers: Map<String, String> = emptyMap(),
    body: JsonObject? = null,
    queryParams: List<Pair<String, String>> = emptyList()
): Pair<T, Map<String, String>> {
    val request = vertxHttpClient.requestAbs(method, url)
    
    headers.forEach { (key, value) ->
        request.putHeader(key, value)
    }
    
    queryParams.forEach { (key, value) ->
        if (value.isNotEmpty()) {
            request.addQueryParam(key, value)
        }
    }
    
    val response = if (body != null) {
        request.sendJsonObject(body).coAwait()
    } else {
        request.send().coAwait()
    }
    
    if (response.statusCode() !in 200..299) {
        throw RuntimeException("HTTP request failed: ${response.statusCode()} ${response.statusMessage()}")
    }
    
    val jsonString = response.bodyAsString()
    try {
        val json = Json { 
            ignoreUnknownKeys = true 
            isLenient = true
            coerceInputValues = true
        }
        val result = json.decodeFromString<T>(jsonString)
        when (result) {
            is EduzzAuthResponse -> result.validate()
            is EduzzGetUserResponse -> result.validate()
            is EduzzGetInvoiceResponse -> result.validate()
            is EduzzGetTaxDocResponse -> result.validate()
            is EduzzLastDaysAmountResponse -> result.validate()
            is EduzzFinancialStatementResponse -> result.validate()
        }
        return Pair(result, response.headers().associate { it.key to it.value })
    } catch (e: Exception) {
        println("Error parsing response: ${e.message}")
        println("Response body: $jsonString")
        throw e
    }
}
