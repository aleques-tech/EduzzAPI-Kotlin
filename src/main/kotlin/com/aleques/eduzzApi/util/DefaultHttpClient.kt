package com.aleques.eduzzApi.util

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal val defaultEduzzApiHttpClientBuilder = OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
    .writeTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS)

internal fun setHttpClient(
    builder: Retrofit.Builder,
    httpClient: OkHttpClient
): Retrofit.Builder {
    return builder.client(httpClient)
}
