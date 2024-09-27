package com.currency.converter
import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import com.currency.converter.feature_currency_converter.data.model.Rates
import com.currency.converter.feature_currency_converter.domain.CurrencyRateUseCase
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import com.currency.converter.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class CurrencyRateUseCaseTest {

    @Mock
    private lateinit var repository: CurrencyRepository
    private lateinit var currencyRateUseCase: CurrencyRateUseCase

    @Before
    fun setUp() {
        //repository = mock(CurrencyRepository::class.java)
        MockitoAnnotations.openMocks(this)
        currencyRateUseCase = CurrencyRateUseCase(repository)

    }

    @Test
    fun `test API success - should emit loading and success with currency list`() = runTest {
        val currencyRateData = CurrencyRateData(
            rates = Rates(
                AED = "1.0",
                AFN = "0.85"
            )
        )

        val apiResponse = Response.success(currencyRateData)

        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenReturn(apiResponse)

        val result = currencyRateUseCase("testId").toList()

        assert(result[0] is Resource.Loading)
        assert(result[1] is Resource.Success)

        val successResource = result[1] as Resource.Success

        assertEquals("AED", successResource.data?.get(0)?.currencyCode)
        assertEquals("AFN", successResource.data?.get(1)?.currencyCode)

    }

    @Test
    fun `test API failure and DB fallback - should emit error and fallback to database`() =
        runTest {
            val apiResponse = Response.error<CurrencyRateData>(500, mock())

            `when`(repository.isCurrencyTableEmpty()).thenReturn(false)
            `when`(repository.isDataStale()).thenReturn(true)
            `when`(repository.getCurrencyRates(anyString())).thenReturn(apiResponse)

            val dbCurrencies = listOf(
                Currency("USD", "1.0"),
                Currency("EUR", "0.85")
            )
            `when`(repository.getAllCurrencies()).thenReturn(dbCurrencies)

            val flow = currencyRateUseCase("testAppId").toList()

            assertTrue(flow[0] is Resource.Loading)
            assertTrue(flow[1] is Resource.Success)

            val successResource = flow[1] as Resource.Success

            assertEquals(2, successResource.data?.size)
            assertEquals("USD", successResource.data?.get(0)?.currencyCode)
            assertEquals("EUR", successResource.data?.get(1)?.currencyCode)
    }

    @Test
    fun `test API and DB both fail - should emit error`() = runTest {
        val apiResponse = Response.error<CurrencyRateData>(500, mock())

        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenReturn(apiResponse)
        `when`(repository.getAllCurrencies()).thenReturn(emptyList())

        val flow = currencyRateUseCase("testAppId").toList()

        assertTrue(flow[0] is Resource.Loading)
        assertTrue(flow[1] is Resource.Error)

        val errorResource = flow[1] as Resource.Error
        assertEquals("API call failed with error: Response malfunction", errorResource.message)
    }
}
