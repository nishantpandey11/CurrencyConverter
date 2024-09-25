package com.currency.converter.domain

import com.currency.converter.data.local.Currency
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.data.model.Rates
import com.currency.converter.utils.AppLogger
import com.currency.converter.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class CurrencyRateUseCase @Inject constructor(private val repository: CurrencyRepository) {
    private val TAG = "CurrencyRateUseCase"
    /**
     * Retrieves all currencies from the Api call/ db dependent on condition.
     *
     * @return A list of all currencies.
     */
    operator fun invoke(appId: String): Flow<Resource<List<Currency>>> = flow {
        /* emit(Resource.Loading())
         if (repository.isCurrencyTableEmpty() || repository.isDataStale()) {
             val apiData = repository.getCurrencyRates(appId)
             apiData.data?.let { persistResponse(it) }
             emit(Resource.Success(apiData.data!!.rates!!.currencies))
             AppLogger.e(TAG,"api call")
         } else {
             val dbData = repository.getAllCurrencies()
             emit(Resource.Success(dbData))
             AppLogger.e(TAG,"db call")
         }*/


        try {
            emit(Resource.Loading())

            if (repository.isCurrencyTableEmpty() || repository.isDataStale()) {
                // Attempt to fetch the data from the API
                when (val apiData = repository.getCurrencyRates(appId)) {
                    is Resource.Success -> {
                        apiData.data?.let {
                            // Persist the data in the local database
                            persistResponse(it)
                            // Emit the successful API response with currency data
                            emit(Resource.Success(it.rates!!.currencies))
                            AppLogger.e(TAG, "API call successful")
                        }
                            ?: emit(Resource.Error("Error: Data is null")) // Handle case where API returns null data
                    }

                    is Resource.Error -> {
                        AppLogger.e(TAG, "API call failed: ${apiData.message}")

                        // If API call fails, try fetching from the local database
                        if (!repository.isCurrencyTableEmpty()) {
                            emit(fetchDataFromDatabase())
                            AppLogger.e(TAG, "Fetched from DB after API failure")
                        } else {
                            // Emit an error if both API and DB fail
                            emit(
                                Resource.Error(
                                    apiData.message ?: "Unknown Error and no data in DB"
                                )
                            )
                            AppLogger.e(TAG, "API and DB both failed: ${apiData.message}")
                        }
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading())
                    }
                }
            } else {
                emit(fetchDataFromDatabase())
                AppLogger.e(TAG, "DB call successful (no need for API)")
            }

        } catch (e: Exception) {
            // Handle any unexpected exceptions and emit an appropriate error message
            emit(Resource.Error("Unexpected Error: ${e.localizedMessage}"))
            AppLogger.e(TAG, "Error: ${e.localizedMessage}")
        }


    }

    // Helper function to fetch data from the database
    private suspend fun fetchDataFromDatabase(apiErrorMessage: String? = null): Resource<List<Currency>> {
        return try {
            val dbData = repository.getAllCurrencies()
            if (dbData.isNotEmpty()) {
                Resource.Success(dbData)
            } else {
                Resource.Error(apiErrorMessage ?: "No data in DB")
            }
        } catch (e: Exception) {
            Resource.Error("Database Error: ${e.localizedMessage}")
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