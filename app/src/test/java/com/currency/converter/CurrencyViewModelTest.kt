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
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.math.RoundingMode

@ExperimentalCoroutinesApi
class CurrencyViewModelTest {

    // Mock dependencies
    private lateinit var currencyRateUseCase: CurrencyRateUseCase
    private lateinit var currencyCodeUseCase: CurrencyCodeUseCase

    // ViewModel under test
    private lateinit var viewModel: CurrencyViewModel

    // Test dispatcher and scope
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        currencyRateUseCase = mock(CurrencyRateUseCase::class.java)
        currencyCodeUseCase = mock(CurrencyCodeUseCase::class.java)

        // Initialize ViewModel
        viewModel = CurrencyViewModel(currencyRateUseCase, currencyCodeUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun `test onSubmitClick with valid input updates currencyValueState`() = runBlockingTest {
        // Given
        val mockObserver = mock(Observer::class.java) as Observer<List<Currency>>
        viewModel.currencyValueState.observeForever(mockObserver)

        val exchangeRateList = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85")
        )
        val amount = "100"
        val selectedCurrencyPosition = 0

        // When
        viewModel.onSubmitClick(amount, exchangeRateList, selectedCurrencyPosition)

        // Then
        val expectedList = viewModel.currencyValueState.value
        verify(mockObserver).onChanged(expectedList!!)
        assert(expectedList.size == 2) // Check if two currencies are returned
    }

    @Test
    fun `test onSubmitClick with empty amount triggers validationState`() = runBlockingTest {
        // Given
        val mockObserver = mock(Observer::class.java) as Observer<Int>
        viewModel.validationState.observeForever(mockObserver)

        val exchangeRateList = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85")
        )
        val amount = ""

        // When
        viewModel.onSubmitClick(amount, exchangeRateList, 0)

        // Then
        verify(mockObserver).onChanged(R.string.txt_valid_amount)
    }

    @Test
    fun `test onSubmitClick with empty exchange rate list triggers validationState`() =
        runBlockingTest {
            // Given
            val mockObserver = mock(Observer::class.java) as Observer<Int>
            viewModel.validationState.observeForever(mockObserver)

            val exchangeRateList = emptyList<Currency>()
            val amount = "100"

            // When
            viewModel.onSubmitClick(amount, exchangeRateList, 0)

            // Then
            verify(mockObserver).onChanged(R.string.txt_empty_list)
        }

    @Test
    fun `test getAllCurrencies updates currencyListState`() = runBlockingTest {
        // Given
        val currencies = listOf("USD", "EUR")
        `when`(currencyCodeUseCase.invoke()).thenReturn(flow { emit(Resource.Success(currencies)) })

        // When
        viewModel.getAllCurrencies()

        // Then
        val expectedState = viewModel.currencyListState.value
        assert(expectedState is Resource.Success && expectedState.data == currencies)
    }

    @Test
    fun `test getExchangeRate updates exchangeRateState`() = runBlockingTest {
        // Given
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

        // When
        viewModel.getExchangeRate("testAppId")

        // Then
        val expectedState = viewModel.exchangeRateState.value
        assert(expectedState is Resource.Success && expectedState.data == exchangeRates)
    }

    @Test
    fun `test getExchangeRate with error updates exchangeRateState`() = runBlockingTest {
        // Given
        `when`(currencyRateUseCase.invoke(anyString())).thenReturn(flow { emit(Resource.Error("Error fetching rates")) })

        // When
        viewModel.getExchangeRate("testAppId")

        // Then
        val expectedState = viewModel.exchangeRateState.value
        assert(expectedState is Resource.Error && expectedState.message == "Error fetching rates")
    }

    @Test
    fun `test getCurrencyValue with valid inputs`() {
        // Arrange
        val currencies = listOf(
            Currency("USD", "1.0"),
            Currency("EUR", "0.85"),
            Currency("INR", "74.0")
        )
        val selectedCurrency = 1 // EUR
        val amount = 100.0

        // Act
        val result = viewModel.getCurrencyValue(selectedCurrency, amount, currencies)

        // Assert
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
        // Arrange
        val currencies = listOf(
            Currency("USD", "0.0"), // Invalid exchange rate (0)
            Currency("EUR", "0.85"),
            Currency("INR", "74.0")
        )
        val selectedCurrency = 0 // USD (with an exchange rate of 0.0)
        val amount = 100.0

        // Act
        viewModel.getCurrencyValue(selectedCurrency, amount, currencies)

        // Assert
        // Exception should be thrown due to division by zero
    }
}
