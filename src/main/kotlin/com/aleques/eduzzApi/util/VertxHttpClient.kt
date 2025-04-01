package com.aleques.eduzzApi.util

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.ValidationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration

internal val vertx = Vertx.vertx()

internal inline fun <reified T> validateSchema(value: T) {
    when (value) {
        is EduzzAuthResponse -> validateAuthResponse(value)
        is EduzzGetUserResponse -> validateUserResponse(value)
        is EduzzGetInvoiceResponse -> validateInvoiceResponse(value)
        is EduzzGetTaxDocResponse -> validateTaxDocResponse(value)
        is EduzzLastDaysAmountResponse -> validateLastDaysAmountResponse(value)
        is EduzzFinancialStatementResponse -> validateFinancialStatementResponse(value)
        else -> throw ValidationException("Unknown response type")
    }
}

internal fun validateAuthResponse(response: EduzzAuthResponse) {
    if (response.data?.get("token").isNullOrEmpty()) {
        throw ValidationException("Invalid auth response: missing token")
    }
}

internal fun validateUserResponse(response: EduzzGetUserResponse) {
    if (response.data.isEmpty()) {
        throw ValidationException("Invalid user response: empty data")
    }
}

internal fun validateInvoiceResponse(response: EduzzGetInvoiceResponse) {
    if (response.data.any { it.sale_id <= 0 }) {
        throw ValidationException("Invalid invoice response: invalid sale_id")
    }
}

internal fun validateTaxDocResponse(response: EduzzGetTaxDocResponse) {
    if (response.data.document_id == null || response.data.document_id!! <= 0) {
        throw ValidationException("Invalid tax doc response: invalid document_id")
    }
}

internal fun validateLastDaysAmountResponse(response: EduzzLastDaysAmountResponse) {
    if (response.data.any { it.date > LocalDate.now() }) {
        throw ValidationException("Invalid last days amount response: future date")
    }
}

internal fun validateFinancialStatementResponse(response: EduzzFinancialStatementResponse) {
    if (response.data.any { it.statement_value < 0 }) {
        throw ValidationException("Invalid financial statement response: negative value")
    }
}

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
): T {
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
        request.sendJsonObject(body).await()
    } else {
        request.send().await()
    }
    
    if (response.statusCode() !in 200..299) {
        throw RuntimeException("HTTP request failed: ${response.statusCode()} ${response.statusMessage()}")
    }
    
    val jsonString = response.bodyAsString()
    val result = Json.decodeFromString<T>(jsonString)
    validateSchema(result)
    return result
}
