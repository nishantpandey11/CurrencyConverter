package com.currency.converter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.domain.CurrencyCodeUseCase
import com.currency.converter.feature_currency_converter.domain.CurrencyRateUseCase
import com.currency.converter.feature_currency_converter.presentation.viewmodel.CurrencyViewModel
import com.currency.converter.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.math.BigDecimal
import java.math.RoundingMode

@ExperimentalCoroutinesApi
class CurrencyViewModelTest {

    @Mock
    private lateinit var currencyRateUseCase: CurrencyRateUseCase

    @Mock
    private lateinit var currencyCodeUseCase: CurrencyCodeUseCase

    private lateinit var viewModel: CurrencyViewModel

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = CurrencyViewModel(currencyRateUseCase, currencyCodeUseCase)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun `test onSubmitClick with valid input updates currencyValueState`() = runBlockingTest {

        val mockObserver = mock(Observer::class.java) as Observer<List<Currency>>
        viewModel.currencyValueState.observeForever(mockObserver)

        val exchangeRateList = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85")
        )
        val amount = "100"
        val selectedCurrencyPosition = 0


        viewModel.onSubmitClick(amount, exchangeRateList, selectedCurrencyPosition)


        val expectedList = viewModel.currencyValueState.value
        verify(mockObserver).onChanged(expectedList!!)
        assert(expectedList.size == 2) // Check if two currencies are returned
    }

    @Test
    fun `test onSubmitClick with empty amount triggers validationState`() = runBlockingTest {

        val mockObserver = mock(Observer::class.java) as Observer<Int>
        viewModel.validationState.observeForever(mockObserver)

        val exchangeRateList = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85")
        )
        val amount = ""

        viewModel.onSubmitClick(amount, exchangeRateList, 0)

        verify(mockObserver).onChanged(R.string.txt_valid_amount)
    }

    @Test
    fun `test onSubmitClick with empty exchange rate list triggers validationState`() =
        runBlockingTest {
            val mockObserver = mock(Observer::class.java) as Observer<Int>
            viewModel.validationState.observeForever(mockObserver)

            val exchangeRateList = emptyList<Currency>()
            val amount = "100"

            viewModel.onSubmitClick(amount, exchangeRateList, 0)

            verify(mockObserver).onChanged(R.string.txt_empty_list)
        }

    @Test
    fun `test getAllCurrencies updates currencyListState`() = runBlockingTest {
        val currencies = listOf("USD", "EUR")
        `when`(currencyCodeUseCase.invoke()).thenReturn(flow { emit(Resource.Success(currencies)) })

        viewModel.getAllCurrencies()

        val expectedState = viewModel.currencyListState.value
        assert(expectedState is Resource.Success && expectedState.data == currencies)
    }

    @Test
    fun `test getExchangeRate updates exchangeRateState`() = runBlockingTest {
        val exchangeRates = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85")
        )
        `when`(currencyRateUseCase.invoke(anyString())).thenReturn(flow {
            emit(
                Resource.Success(
                    exchangeRates
                )
            )
        })

        viewModel.getExchangeRate("testAppId")

        val expectedState = viewModel.exchangeRateState.value
        assert(expectedState is Resource.Success && expectedState.data == exchangeRates)
    }

    @Test
    fun `test getExchangeRate with error updates exchangeRateState`() = runBlockingTest {
        `when`(currencyRateUseCase.invoke(anyString())).thenReturn(flow { emit(Resource.Error("Error fetching rates")) })

        viewModel.getExchangeRate("testAppId")

        val expectedState = viewModel.exchangeRateState.value
        assert(expectedState is Resource.Error && expectedState.message == "Error fetching rates")
    }

    @Test
    fun `test getCurrencyValue with valid inputs`() {
        val currencies = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85"),
            Currency("INR", "74.0")
        )
        val selectedCurrency = 1 // EUR
        val amount = 100.0

        val result = viewModel.getCurrencyValue(selectedCurrency, amount, currencies)

        // Step 1: Calculate amountInUsd (amount in EUR converted to USD)
        val amountInUsd = BigDecimal(amount).divide(
            currencies[selectedCurrency].exchangeRate.toBigDecimal(), 10, RoundingMode.HALF_UP
        )

        // Step 2: Verify that the new exchange rates are correctly calculated
        val expectedUsdExchangeRate = amountInUsd.multiply(currencies[0].exchangeRate.toBigDecimal())
            .setScale(3, RoundingMode.HALF_UP).toPlainString()
        val expectedEurExchangeRate = amountInUsd.multiply(currencies[1].exchangeRate.toBigDecimal())
            .setScale(3, RoundingMode.HALF_UP).toPlainString()
        val expectedInrExchangeRate = amountInUsd.multiply(currencies[2].exchangeRate.toBigDecimal())
            .setScale(3, RoundingMode.HALF_UP).toPlainString()

        assertEquals(expectedUsdExchangeRate, result[0].exchangeRate)
        assertEquals(expectedEurExchangeRate, result[1].exchangeRate)
        assertEquals(expectedInrExchangeRate, result[2].exchangeRate)
    }

    @Test(expected = ArithmeticException::class)
    fun `test getCurrencyValue with division by zero`() {

        val currencies = listOf(
            Currency("USD", "0.0"), // Invalid exchange rate (0)
            Currency("EUR", "0.85"),
            Currency("INR", "74.0")
        )
        val selectedCurrency = 0 // USD (with an exchange rate of 0.0)
        val amount = 100.0

        viewModel.getCurrencyValue(selectedCurrency, amount, currencies)

    }
}
