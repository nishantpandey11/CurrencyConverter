package com.currency.converter.domain.repository

import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.utils.Resource

interface CurrencyRepository {
    suspend fun getCurrencyRates(appID: String): Resource<CurrencyRateData>

}