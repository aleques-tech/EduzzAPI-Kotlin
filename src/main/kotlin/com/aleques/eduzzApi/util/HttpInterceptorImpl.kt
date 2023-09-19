package com.aleques.eduzzApi.util

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal fun getInterceptor(builder: Retrofit.Builder): Retrofit.Builder {
    return builder.client(
        OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
        .writeTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS).build())
}