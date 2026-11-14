package com.checktracker.di

import com.checktracker.data.remote.ExchangeRateApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single<ExchangeRateApi> { get<Retrofit>().create(ExchangeRateApi::class.java) }
}
