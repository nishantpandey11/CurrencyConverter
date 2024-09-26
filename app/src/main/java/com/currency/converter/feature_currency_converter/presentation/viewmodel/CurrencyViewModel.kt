package com.currency.converter.feature_currency_converter.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currency.converter.R
import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.domain.CurrencyCodeUseCase
import com.currency.converter.feature_currency_converter.domain.CurrencyRateUseCase
import com.currency.converter.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.math.RoundingMode
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

    private val _currencyValueState = MutableLiveData<List<Currency>>(emptyList())
    val currencyValueState = _currencyValueState

    private val _validationState = MutableLiveData<Int>()
    val validationState = _validationState


    fun onSubmitClick(
        amount: String,
        exchangeRateList: List<Currency>,
        selectedCurrencyPosition: Int
    ) {
        if (amount.isEmpty()) {
            _validationState.value = R.string.txt_valid_amount
            return
        }

        if (exchangeRateList.isEmpty()) {
            _validationState.value = R.string.txt_empty_list
            return
        }

        val amountValueInOtherCurrenciesList = getCurrencyValue(
            selectedCurrencyPosition,
            amount.toDouble(),
            exchangeRateList
        )

        _currencyValueState.value = amountValueInOtherCurrenciesList
    }


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

    private fun getCurrencyValue(
        selectedCurrency: Int,
        amount: Double,
        currencies: List<Currency>
    ): List<Currency> {

        val amountInUsd = amount.toBigDecimal()
            .divide(
                currencies[selectedCurrency].exchangeRate.toBigDecimal(),
                10,
                RoundingMode.HALF_UP
            )


        return currencies.map {
            it.copy(
                exchangeRate = amountInUsd
                    .multiply(it.exchangeRate.toBigDecimal())
                    .setScale(3, RoundingMode.HALF_UP)
                    .toPlainString()
            )
        }
    }


}