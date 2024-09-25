package com.currency.converter.data

import com.currency.converter.data.local.Currency
import com.currency.converter.data.local.CurrencyDao
import com.currency.converter.data.local.DataStoreManager
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.data.remote.ApiService
import com.currency.converter.domain.CurrencyRepository
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

    override suspend fun upsertCurrency(currency: Currency) {
        withContext(context = Dispatchers.IO) {
            currencyDao.upsertCurrency(currency = currency)
        }
    }

    override suspend fun upsertCurrencies(currencies: List<Currency>) {
        withContext(context = Dispatchers.IO) {
            currencyDao.upsertCurrencies(currencies = currencies)
        }
    }

    override suspend fun updateExchangeRates(currencies: List<Currency>) {
        withContext(context = Dispatchers.IO) {
            currencyDao.updateExchangeRates(currencies = currencies)
        }
    }

    override suspend fun getCurrency(currencyCode: String): Currency {
        return withContext(context = Dispatchers.IO) {
            currencyDao.getCurrency(currencyCode = currencyCode)
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


    override suspend fun setFirstLaunch(value: Boolean) {
        withContext(context = Dispatchers.IO) {
            dataStoreManager.setFirstLaunch(value = value)
        }
    }

    override suspend fun isFirstLaunch(): Boolean {
        return withContext(context = Dispatchers.IO) {
            dataStoreManager.isFirstLaunch()
        }
    }

    override suspend fun setTimestampInSeconds(value: Long) {
        withContext(context = Dispatchers.IO) {
            dataStoreManager.setTimestampInSeconds(value = value)
        }
    }

    override suspend fun getTimestampInSeconds(): Long {
        return withContext(context = Dispatchers.IO) {
            dataStoreManager.getTimestampInSeconds()
        }
    }

    override suspend fun isDataEmpty(): Boolean {
        return withContext(context = Dispatchers.IO) {
            dataStoreManager.isDataEmpty()
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