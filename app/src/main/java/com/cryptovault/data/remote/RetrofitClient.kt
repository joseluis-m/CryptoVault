package com.cryptovault.data.remote

import com.cryptovault.data.remote.api.CoinGeckoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit configurado como Singleton.
 *
 * Incluye:
 * - Logging interceptor para depuración de peticiones HTTP.
 * - Timeouts configurados para evitar bloqueos.
 * - Base URL de CoinGecko API (versión gratuita).
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.coingecko.com/api/v3/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: CoinGeckoApi = retrofit.create(CoinGeckoApi::class.java)
}
