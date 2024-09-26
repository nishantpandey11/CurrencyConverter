package com.currency.converter

import com.currency.converter.feature_currency_converter.data.CurrencyRepositoryImpl
import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.data.local.CurrencyDao
import com.currency.converter.feature_currency_converter.data.local.DataStoreManager
import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import com.currency.converter.feature_currency_converter.data.remote.ApiService
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class CurrencyRepositoryImplTest {

    // Mock dependencies
    private lateinit var apiService: ApiService
    private lateinit var currencyDao: CurrencyDao
    private lateinit var dataStoreManager: DataStoreManager

    // Repository under test
    private lateinit var currencyRepository: CurrencyRepository

    // Test dispatcher for coroutines
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        apiService = mock(ApiService::class.java)
        currencyDao = mock(CurrencyDao::class.java)
        dataStoreManager = mock(DataStoreManager::class.java)

        currencyRepository = CurrencyRepositoryImpl(apiService, currencyDao, dataStoreManager)

        // Set the test dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Clean up the dispatcher after tests
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test getCurrencyRates success`() = runBlocking {
        // Given
        val appID = "testAppID"
        val currencyRateData = mock(CurrencyRateData::class.java)
        val response = mock(Response::class.java) as Response<CurrencyRateData>

        // When
        `when`(apiService.getCurrencyRates(appID)).thenReturn(response)

        // Call the method
        val result = currencyRepository.getCurrencyRates(appID)

        // Then
        verify(apiService, times(1)).getCurrencyRates(appID)
        assert(result == response)
    }

    @Test
    fun `test upsertCurrencies`() = runBlocking {
        // Given
        val currencies = listOf(Currency("USD",  "1.0"), Currency("EUR", "0.85"))

        // Call the method
        currencyRepository.upsertCurrencies(currencies)

        // Then
        verify(currencyDao, times(1)).upsertCurrencies(currencies)
    }

    @Test
    fun `test getAllCurrencies`() = runBlocking {
        // Given
        val currencies = listOf(Currency("USD", "1.0"), Currency("EUR",  "0.85"))
        `when`(currencyDao.getAllCurrencies()).thenReturn(currencies)

        // Call the method
        val result = currencyRepository.getAllCurrencies()

        // Then
        verify(currencyDao, times(1)).getAllCurrencies()
        assert(result == currencies)
    }

    @Test
    fun `test getAllCurrencyCodes`() = runBlocking {
        // Given
        val currencyCodes = listOf("USD", "EUR")
        `when`(currencyDao.getAllCurrencyCodes()).thenReturn(currencyCodes)

        // Call the method
        val result = currencyRepository.getAllCurrencyCodes()

        // Then
        verify(currencyDao, times(1)).getAllCurrencyCodes()
        assert(result == currencyCodes)
    }

    @Test
    fun `test setTimestampInSeconds`() = runBlocking {
        // Given
        val timestamp = 123456789L

        // Call the method
        currencyRepository.setTimestampInSeconds(timestamp)

        // Then
        verify(dataStoreManager, times(1)).setTimestampInSeconds(timestamp)
    }

    @Test
    fun `test isDataStale`() = runBlocking {
        // Given
        `when`(dataStoreManager.isDataStale()).thenReturn(true)

        // Call the method
        val result = currencyRepository.isDataStale()

        // Then
        verify(dataStoreManager, times(1)).isDataStale()
        assert(result)
    }

    @Test
    fun `test isCurrencyTableEmpty returns true`() = runBlocking {
        // Given
        `when`(currencyDao.getCurrencyCount()).thenReturn(0)

        // Call the method
        val result = currencyRepository.isCurrencyTableEmpty()

        // Then
        verify(currencyDao, times(1)).getCurrencyCount()
        assert(result)
    }

    @Test
    fun `test isCurrencyTableEmpty returns false`() = runBlocking {
        // Given
        `when`(currencyDao.getCurrencyCount()).thenReturn(1)

        // Call the method
        val result = currencyRepository.isCurrencyTableEmpty()

        // Then
        verify(currencyDao, times(1)).getCurrencyCount()
        assert(!result)
    }
}
