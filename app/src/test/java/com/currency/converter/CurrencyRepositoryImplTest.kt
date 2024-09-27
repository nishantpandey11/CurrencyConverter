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
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class CurrencyRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var currencyDao: CurrencyDao

    @Mock
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var currencyRepository: CurrencyRepository

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        currencyRepository = CurrencyRepositoryImpl(apiService, currencyDao, dataStoreManager)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test getCurrencyRates success`() = runBlocking {
        val appID = "testAppID"
        val currencyRateData = mock(CurrencyRateData::class.java)
        val response = mock(Response::class.java) as Response<CurrencyRateData>

        `when`(apiService.getCurrencyRates(appID)).thenReturn(response)

        val result = currencyRepository.getCurrencyRates(appID)

        verify(apiService, times(1)).getCurrencyRates(appID)
        assert(result == response)
    }

    @Test
    fun `test upsertCurrencies`() = runBlocking {
        val currencies = listOf(Currency("USD",  "1.0"), Currency("EUR", "0.85"))
        currencyRepository.upsertCurrencies(currencies)
        verify(currencyDao, times(1)).upsertCurrencies(currencies)
    }

    @Test
    fun `test getAllCurrencies`() = runBlocking {
        val currencies = listOf(Currency("USD", "1.0"), Currency("EUR",  "0.85"))
        `when`(currencyDao.getAllCurrencies()).thenReturn(currencies)

        val result = currencyRepository.getAllCurrencies()
        verify(currencyDao, times(1)).getAllCurrencies()
        assert(result == currencies)
    }

    @Test
    fun `test getAllCurrencyCodes`() = runBlocking {
        val currencyCodes = listOf("USD", "EUR")
        `when`(currencyDao.getAllCurrencyCodes()).thenReturn(currencyCodes)

        val result = currencyRepository.getAllCurrencyCodes()
        verify(currencyDao, times(1)).getAllCurrencyCodes()
        assert(result == currencyCodes)
    }

    @Test
    fun `test setTimestampInSeconds`() = runBlocking {
        val timestamp = 123456789L
        currencyRepository.setTimestampInSeconds(timestamp)
        verify(dataStoreManager, times(1)).setTimestampInSeconds(timestamp)
    }

    @Test
    fun `test isDataStale`() = runBlocking {
        `when`(dataStoreManager.isDataStale()).thenReturn(true)
        val result = currencyRepository.isDataStale()
        verify(dataStoreManager, times(1)).isDataStale()
        assert(result)
    }

    @Test
    fun `test isCurrencyTableEmpty returns true`() = runBlocking {
        `when`(currencyDao.getCurrencyCount()).thenReturn(0)
        val result = currencyRepository.isCurrencyTableEmpty()
        verify(currencyDao, times(1)).getCurrencyCount()
        assert(result)
    }

    @Test
    fun `test isCurrencyTableEmpty returns false`() = runBlocking {
        `when`(currencyDao.getCurrencyCount()).thenReturn(1)
        val result = currencyRepository.isCurrencyTableEmpty()
        verify(currencyDao, times(1)).getCurrencyCount()
        assert(!result)
    }
}
