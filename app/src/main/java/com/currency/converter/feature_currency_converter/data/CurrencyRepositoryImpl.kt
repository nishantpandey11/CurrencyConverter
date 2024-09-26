package com.currency.converter.feature_currency_converter.data

import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.data.local.CurrencyDao
import com.currency.converter.feature_currency_converter.data.local.DataStoreManager
import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import com.currency.converter.feature_currency_converter.data.remote.ApiService
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val currencyDao: CurrencyDao,
    private val dataStoreManager: DataStoreManager,
) : CurrencyRepository {
    override suspend fun getCurrencyRates(appID: String): Response<CurrencyRateData> {
        return withContext(Dispatchers.IO) {
            apiService.getCurrencyRates(appID)
        }
    }


    override suspend fun upsertCurrencies(currencies: List<Currency>) {
        withContext(context = Dispatchers.IO) {
            currencyDao.upsertCurrencies(currencies = currencies)
        }
    }


    override suspend fun getAllCurrencies(): List<Currency> {
        return withContext(context = Dispatchers.IO) {
            currencyDao.getAllCurrencies()
        }
    }

    override suspend fun getAllCurrencyCodes(): List<String> {
        return withContext(context = Dispatchers.IO) {
            currencyDao.getAllCurrencyCodes()
        }
    }


    override suspend fun setTimestampInSeconds(value: Long) {
        withContext(context = Dispatchers.IO) {
            dataStoreManager.setTimestampInSeconds(value = value)
        }
    }


    override suspend fun isDataStale(): Boolean {
        return withContext(context = Dispatchers.IO) {
            dataStoreManager.isDataStale()
        }
    }

    override suspend fun isCurrencyTableEmpty(): Boolean {
        return withContext(context = Dispatchers.IO) {
            currencyDao.getCurrencyCount() == 0
        }
    }
}