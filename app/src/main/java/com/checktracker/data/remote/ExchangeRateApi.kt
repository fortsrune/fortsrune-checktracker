package com.checktracker.data.remote

import com.checktracker.data.remote.dto.RatesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun getRates(@Path("base") base: String): RatesResponse
}
