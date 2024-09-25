package com.currency.converter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currency.converter.data.local.Currency
import com.currency.converter.domain.CurrencyCodeUseCase
import com.currency.converter.domain.CurrencyRateUseCase
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

    private val _currencyListState = MutableStateFlow<Resource<List<String>>>(Resource.Loading())
    val currencyListState = _currencyListState.asStateFlow()

    private val _exchangeRateState = MutableStateFlow<Resource<List<Currency>>>(Resource.Loading())
    val exchangeRateState = _exchangeRateState.asStateFlow()

    fun getAllCurrencies() {
        currencyCodeUseCase().onEach {
            _currencyListState.value = it
        }.launchIn(viewModelScope)
    }

    fun getExchangeRate(appId: String) {
        currencyRateUseCase(appId).onEach {
            _exchangeRateState.value = it
        }.launchIn(viewModelScope)
    }

    fun getCurrencyValue(
        selectedCurrency: Int,
        amount: Double,
        currencies: List<Currency>
    ): List<Currency> {

        val amountInUsd = amount / currencies[selectedCurrency].exchangeRate
        return currencies.map {
            it.copy(exchangeRate = String.format("%.3f", amountInUsd * it.exchangeRate).toDouble())
        }
    }

}