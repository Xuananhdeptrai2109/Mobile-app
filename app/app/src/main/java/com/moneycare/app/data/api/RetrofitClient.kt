package com.moneycare.app.data.api

import com.moneycare.app.MoneyCareApp
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Base URL configurable via BuildConfig: use emulator host for debug, real IP for release
    private const val BASE_URL = com.moneycare.app.BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (com.moneycare.app.BuildConfig.DEBUG) {
            runCatching {
                HttpLoggingInterceptor.Level.valueOf(com.moneycare.app.BuildConfig.API_LOG_LEVEL.uppercase())
            }.getOrDefault(HttpLoggingInterceptor.Level.BASIC)
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val token = try { MoneyCareApp.instance.sessionManager.getToken() } catch (e: Throwable) { null }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        if (response.code == 401 && token != null) {
            try {
                MoneyCareApp.instance.sessionManager.clearSession()
            } catch (_: Throwable) {
                // The app instance is unavailable only in isolated previews/tests.
            }
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(com.moneycare.app.BuildConfig.API_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(com.moneycare.app.BuildConfig.API_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
