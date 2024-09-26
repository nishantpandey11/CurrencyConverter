package com.currency.converter.feature_currency_converter.data.remote

import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/api/latest.json")
    suspend fun getCurrencyRates(@Query("app_id") appID: String): Response<CurrencyRateData>
}