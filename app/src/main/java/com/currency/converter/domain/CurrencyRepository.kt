package com.currency.converter.domain

import com.currency.converter.data.local.Currency
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.utils.Resource
import kotlinx.coroutines.Dispatchers

interface CurrencyRepository {
    suspend fun getCurrencyRates(appID: String): Resource<CurrencyRateData>
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