package com.currency.converter.domain

import com.currency.converter.data.local.Currency
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.utils.AppLogger
import com.currency.converter.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class CurrencyRateUseCase @Inject constructor(private val repository: CurrencyRepository) {
    private val TAG = "CurrencyRateUseCase"

    /**
     * Retrieves all currencies from the Api call/ db dependent on condition.
     *
     * @return A list of all currencies.
     */
    /*
    operator fun invoke(appId: String): Flow<Resource<List<Currency>>> = flow {


        //working code

        /* try {
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
         }*/

        //

    }

     */

    operator fun invoke(appId: String): Flow<Resource<List<Currency>>> = flow {
        emit(Resource.Loading())

        try {
            val isTableEmpty = repository.isCurrencyTableEmpty()
            val isDataStale = repository.isDataStale()

            // Check if data should be fetched from API or database
            if (isTableEmpty || isDataStale) {
                val res = repository.getCurrencyRates(appId)

                if (res.isSuccessful) {
                    res.body()?.let { response ->
                        persistResponse(response) // Persist the data in local DB
                        emit(Resource.Success(response.rates!!.currencies)) // Emit success
                        AppLogger.e(TAG, "API call successful")
                    } ?: run {
                        handleFetchError("API response body is null")
                    }
                } else {
                    handleFetchError("API call failed with error: ${res.message()}", isTableEmpty)
                }
            } else {
                emit(fetchDataFromDatabase()) // Fetch from DB when data is not stale
                AppLogger.e(TAG, "DB call successful (no need for API)")
            }
        } catch (e: Exception) {
            handleFetchError("Exception: ${e.localizedMessage}", repository.isCurrencyTableEmpty())
        }
    }

    private suspend fun FlowCollector<Resource<List<Currency>>>.handleFetchError(
        errorMessage: String, isTableEmpty: Boolean = false
    ) {
        AppLogger.e(TAG, errorMessage)

        // Try to fetch from DB after API failure
        if (!isTableEmpty) {
            emit(fetchDataFromDatabase())
            AppLogger.e(TAG, "Fetched from DB after API failure")
        } else {
            emit(Resource.Error(errorMessage))
            AppLogger.e(TAG, "API and DB both failed")
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


    /*
     * Persists the response payload containing exchange rates and timestamp.
     *
     * @param payload The data payload containing exchange rates and timestamp.
     */

    private suspend fun persistResponse(payload: CurrencyRateData) {
        payload.rates?.let { exchangeRates ->
            repository.upsertCurrencies(currencies = exchangeRates.currencies)
        }
        repository.setTimestampInSeconds(System.currentTimeMillis())

    }

}