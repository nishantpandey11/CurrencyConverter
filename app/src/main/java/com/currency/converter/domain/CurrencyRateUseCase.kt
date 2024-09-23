package com.currency.converter.domain

import android.util.Log
import com.currency.converter.data.local.Currency
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.data.model.Rates
import com.currency.converter.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class CurrencyRateUseCase @Inject constructor(private val repository: CurrencyRepository) {
    /**
     * Retrieves all currencies from the Api call/ db dependent on condition.
     *
     * @return A list of all currencies.
     */
    operator fun invoke(appId: String): Flow<Resource<List<Currency>>> = flow {
        emit(Resource.Loading())
        if (repository.isCurrencyTableEmpty() || repository.isDataStale()) {
            val apiData = repository.getCurrencyRates(appId)
            apiData.data?.let { persistResponse(it) }
            emit(Resource.Success(apiData.data!!.rates!!.currencies))
            Log.e("===>","api call")
        } else {
            val dbData = repository.getAllCurrencies()
            emit(Resource.Success(dbData))
            Log.e("===>","db call")
        }

    }


    /**
     * Persists the response payload containing exchange rates and timestamp.
     *
     * @param payload The data payload containing exchange rates and timestamp.
     */
    private suspend fun persistResponse(payload: CurrencyRateData) {
        withContext(Dispatchers.Default) {
            payload.rates?.let { exchangeRates ->
                persistCurrencies(exchangeRates = exchangeRates)
            }
            persistTimestamp(timestamp = System.currentTimeMillis())//payload.timestamp)
        }
    }

    /**
     * Persists the exchange rates by either inserting new data or updating existing data in the repository.
     *
     * @param exchangeRates The exchange rates to be persisted.
     */
    private suspend fun persistCurrencies(exchangeRates: Rates) {
        withContext(Dispatchers.Default) {
            when {
                repository.isDataEmpty() -> {
                    repository.upsertCurrencies(currencies = exchangeRates.currencies)
                }

                repository.isDataStale() -> {
                    repository.updateExchangeRates(currencies = exchangeRates.currencies)
                }
            }
        }
    }

    /**
     * Persists the timestamp in the repository.
     *
     * @param timestamp The timestamp to be persisted.
     */
    private suspend fun persistTimestamp(timestamp: Long) {
        withContext(Dispatchers.Default) {
            repository.setTimestampInSeconds(value = timestamp)
        }
    }
}