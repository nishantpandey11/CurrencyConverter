package com.currency.converter.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currency.converter.domain.CurrencyCodeUseCase
import com.currency.converter.domain.CurrencyRateUseCase
import com.currency.converter.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRateUseCase: CurrencyRateUseCase,
    private val currencyCodeUseCase: CurrencyCodeUseCase
) : ViewModel() {

    fun getCurrencyRate(appId: String) {

        currencyRateUseCase(appId).onEach {
            when (it) {
                is Resource.Loading -> {
                    Log.e("====>", "Loading...")
                }

                is Resource.Success -> {
                    Log.e("====>", it.data.toString())
                }

                is Resource.Error -> {
                    Log.e("====>", it.message.toString())
                }

            }
        }.launchIn(viewModelScope)


    }

    fun getAllCurrencies() {
        currencyCodeUseCase().onEach {
            when (it) {
                is Resource.Loading -> {
                    Log.e("====>", "Loading...")
                }

                is Resource.Success -> {
                    Log.e("====>", it.data.toString())
                }

                is Resource.Error -> {
                    Log.e("====>", it.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }
}