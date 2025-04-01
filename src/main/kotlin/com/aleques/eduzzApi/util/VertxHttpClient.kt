package com.aleques.eduzzApi.util

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
    
    return Json.decodeFromString(response.bodyAsString())
}
