package com.currency.converter

import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.data.model.CurrencyRateData
import com.currency.converter.feature_currency_converter.data.model.Rates
import com.currency.converter.feature_currency_converter.domain.CurrencyRateUseCase
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import com.currency.converter.utils.Resource
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRateUseCaseTest {

    @Mock
    private lateinit var repository: CurrencyRepository

    private lateinit var useCase: CurrencyRateUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        useCase = CurrencyRateUseCase(repository)
    }

    // Test Case 1: Success - API data fetched when table is empty or data is stale
    @Test
    fun `test data fetched from API when table is empty or data is stale`() = runTest {
        // Mock data setup
        val mockRates = Rates(
            AED = "3.67",
            AFN = "87.50",
            ALL = "102.90"
            // Add more currencies as necessary
        )
        val mockCurrencyRateData = CurrencyRateData(
            disclaimer = "Some disclaimer",
            license = "Some license",
            timestamp = System.currentTimeMillis(),
            base = "USD",
            rates = mockRates
        )

        // Mock repository responses
        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenReturn(
            Response.success(mockCurrencyRateData)
        )

        // Call the use case
        val result = useCase("appId").first()

        // Assertions
        assertTrue(result is Resource.Success)
        val expectedCurrencies = listOf(
            Currency("AED", "3.67"),
            Currency("AFN", "87.50"),
            Currency("ALL", "102.90")
            // Include all expected currencies from mockRates
        )
        assertEquals(expectedCurrencies.size, (result as Resource.Success).data?.size)
        verify(repository).upsertCurrencies(expectedCurrencies)
    }

    // Test Case 2: API Error - Fallback to DB
    @Test
    fun `test fallback to DB when API call fails`() = runTest {
        // Mock data for DB fallback
        val mockDbCurrencies = listOf(
            Currency("AED", "3.67"),
            Currency("AFN", "87.50"),
            Currency("ALL", "102.90")
        )

        // Mock repository responses
        `when`(repository.isCurrencyTableEmpty()).thenReturn(false)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenReturn(
            Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error"))
        )
        `when`(repository.getAllCurrencies()).thenReturn(mockDbCurrencies)

        // Call the use case
        val result = useCase("appId").first()

        // Assertions
        assertTrue(result is Resource.Success)
        assertEquals(mockDbCurrencies.size, (result as Resource.Success).data?.size)
        verify(repository, never()).upsertCurrencies(mockDbCurrencies)
    }

    // Test Case 3: API and DB Both Fail
    @Test
    fun `test API and DB failure`() = runTest {
        // Mock repository responses
        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenReturn(
            Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error"))
        )

        // Call the use case
        val result = useCase("appId").first()

        // Assertions
        assertTrue(result is Resource.Error)
        verify(repository, never()).getAllCurrencies()
    }

    // Test Case 4: Fetch Data from DB when table is not empty and data is not stale
    @Test
    fun `test fetch data from DB when not stale`() = runTest {
        // Mock data for DB fetch
        val mockDbCurrencies = listOf(
            Currency("AED", "3.67"),
            Currency("AFN", "87.50"),
            Currency("ALL", "102.90")
        )

        // Mock repository responses
        `when`(repository.isCurrencyTableEmpty()).thenReturn(false)
        `when`(repository.isDataStale()).thenReturn(false)
        `when`(repository.getAllCurrencies()).thenReturn(mockDbCurrencies)

        // Call the use case
        val result = useCase("appId").first()

        // Debugging: Log the result
        println("Result from use case: $result")

        // Assertions
        assertTrue(result is Resource.Success)

        // Ensure the data returned is not null before checking size
        val actualCurrencies = (result as Resource.Success).data
        assertNotNull(actualCurrencies)  // Ensure it's not null
        assertEquals(mockDbCurrencies.size, actualCurrencies?.size)

        // Additional check to see if the returned currencies match the expected currencies
        assertEquals(mockDbCurrencies, actualCurrencies)
        verify(repository, never()).getCurrencyRates(anyString())
    }

    // Test Case 5: Exception Handling
    @Test
    fun `test exception handling`() = runTest {
        // Arrange
        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        `when`(repository.isDataStale()).thenReturn(true)
        `when`(repository.getCurrencyRates(anyString())).thenThrow(RuntimeException("Test Exception"))

        // Act
        val result = useCase("appId").first()

        // Assert
        assertTrue(result is Resource.Error)
        assertEquals("Exception: Test Exception", (result as Resource.Error).message)
    }
}
