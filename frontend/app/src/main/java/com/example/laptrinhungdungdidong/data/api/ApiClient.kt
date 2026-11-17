package com.example.laptrinhungdungdidong.data.api

import android.os.Build
import com.example.laptrinhungdungdidong.BuildConfig
import com.example.laptrinhungdungdidong.FlashcardApp
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // After deployment, all devices use the same HTTPS API on EC2.
    // Use laptop/emulator HTTP only when publicApiBaseUrl is explicitly set to blank.
    private val BASE_URL: String = when {
        BuildConfig.PUBLIC_API_BASE_URL.isNotBlank() -> BuildConfig.PUBLIC_API_BASE_URL
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) -> "http://10.0.2.2:8080/"
        else -> BuildConfig.PHONE_API_BASE_URL
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = SessionManager(FlashcardApp.instance).getAuthToken()
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
