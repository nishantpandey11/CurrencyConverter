package com.currency.converter.feature_currency_converter.domain

import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import retrofit2.Response

interface CurrencyRepository {
    suspend fun getCurrencyRates(appID: String): Response<CurrencyRateData>
    suspend fun upsertCurrency(currency: Currency)
    suspend fun upsertCurrencies(currencies: List<Currency>)
    suspend fun updateExchangeRates(currencies: List<Currency>)
    suspend fun getCurrency(currencyCode: String): Currency
    suspend fun getAllCurrencies(): List<Currency>
    suspend fun getAllCurrencyCodes(): List<String>
    suspend fun setFirstLaunch(value: Boolean)
    suspend fun isFirstLaunch(): Boolean
    suspend fun setTimestampInSeconds(value: Long)
    suspend fun getTimestampInSeconds(): Long
    suspend fun isDataEmpty(): Boolean
    suspend fun isDataStale(): Boolean
    suspend fun isCurrencyTableEmpty():Boolean

}