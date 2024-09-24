package com.currency.converter.presentation.model

import com.currency.converter.data.local.Currency


sealed class CurrencyListState {
    data object Loading : CurrencyListState()
    data class Error(val message: String) : CurrencyListState()
    data class Success(val currencies: List<String>?) : CurrencyListState()
    data class ExchangeRateSuccess(val exchangeRate: List<Currency>?) : CurrencyListState()
}
