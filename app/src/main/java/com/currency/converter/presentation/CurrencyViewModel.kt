package com.currency.converter.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currency.converter.domain.CurrencyCodeUseCase
import com.currency.converter.domain.CurrencyRateUseCase
import com.currency.converter.presentation.model.CurrencyListState
import com.currency.converter.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRateUseCase: CurrencyRateUseCase,
    private val currencyCodeUseCase: CurrencyCodeUseCase
) : ViewModel() {
    private val _currencyListState = MutableStateFlow<CurrencyListState>(CurrencyListState.Loading)
    val currencyListState = _currencyListState.asStateFlow()

    private val _currencyExchangeRateState =
        MutableStateFlow<CurrencyListState>(CurrencyListState.Loading)
    val currencyExchangeRateState = _currencyExchangeRateState.asStateFlow()

    fun getAllCurrencies() {
        currencyCodeUseCase().onEach {
            when (it) {
                is Resource.Loading -> {
                    _currencyListState.value = CurrencyListState.Loading
                }

                is Resource.Success -> {
                    Log.e("====>", it.data.toString())
                    _currencyListState.value = CurrencyListState.Success(it.data)
                }

                is Resource.Error -> {
                    Log.e("====>", it.message.toString())
                    _currencyListState.value =
                        CurrencyListState.Error(it.message ?: "Something went wrong!")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getExchangeRate(appId: String) {

        currencyRateUseCase(appId).onEach {
            when (it) {
                is Resource.Loading -> {
                    _currencyListState.value = CurrencyListState.Loading
                }

                is Resource.Success -> {
                    _currencyListState.value = CurrencyListState.ExchangeRateSuccess(it.data)
                }

                is Resource.Error -> {
                    _currencyListState.value =
                        CurrencyListState.Error(it.message ?: "Something went wrong!")
                }

            }
        }.launchIn(viewModelScope)


    }


}