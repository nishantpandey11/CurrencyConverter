package com.currency.converter.domain.usecase

import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.domain.repository.CurrencyRepository
import com.currency.converter.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class CurrencyRateUseCase @Inject constructor(private val repository: CurrencyRepository) {
    operator fun invoke(appId: String): Flow<Resource<CurrencyRateData>> = flow {
        emit(Resource.Loading())
        val data = repository.getCurrencyRates(appId)
        emit(data)
    }
}