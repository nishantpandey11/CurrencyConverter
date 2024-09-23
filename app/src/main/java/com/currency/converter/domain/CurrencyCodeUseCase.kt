package com.currency.converter.domain

import com.currency.converter.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class CurrencyCodeUseCase @Inject constructor(private val repository: CurrencyRepository) {
    operator fun invoke(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        if (!repository.isCurrencyTableEmpty()) {
            val data = repository.getAllCurrencyCodes()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Something went wrong"))
        }

    }
}