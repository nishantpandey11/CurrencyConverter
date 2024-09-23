package com.currency.converter.data

import com.currency.converter.data.local.Currency
import com.currency.converter.data.local.CurrencyDao
import com.currency.converter.data.local.DataStoreManager
import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.data.remote.ApiService
import com.currency.converter.domain.CurrencyRepository
import com.currency.converter.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val currencyDao: CurrencyDao,
    private val dataStoreManager: DataStoreManager,
) : CurrencyRepository {
    override suspend fun getCurrencyRates(appID: String): Resource<CurrencyRateData> {
        return try {
            val res = apiService.getCurrencyRates(appID)
            if (res.isSuccessful) {
                res.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Unknown Error Occurred")
            } else
                Resource.Error(res.message())
        } catch (e: HttpException) {
            Resource.Error("Unexpected HttpException " + e.localizedMessage)
        } catch (e: IOException) {
            Resource.Error("IO Exception, couldn't reach server " + e.localizedMessage)
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
        return currencyDao.getCurrencyCount() == 0
    }



}