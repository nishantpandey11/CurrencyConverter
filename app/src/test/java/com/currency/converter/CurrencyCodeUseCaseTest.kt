package com.currency.converter

import com.currency.converter.feature_currency_converter.domain.CurrencyCodeUseCase
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import com.currency.converter.utils.Resource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CurrencyCodeUseCaseTest {

    @Mock
    private lateinit var repository: CurrencyRepository
    private lateinit var useCase: CurrencyCodeUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        useCase = CurrencyCodeUseCase(repository)
    }

    @Test
    fun `invoke should return Success when currency table is not empty`() = runTest {
        val currencyCodes = listOf("USD", "EUR", "INR")
        `when`(repository.isCurrencyTableEmpty()).thenReturn(false)
        `when`(repository.getAllCurrencyCodes()).thenReturn(currencyCodes)

        val result = useCase().toList()

        assert(result[0] is Resource.Loading)
        assert(result[1] is Resource.Success)
        assertEquals((result[1] as Resource.Success).data, currencyCodes)

        verify(repository).isCurrencyTableEmpty()
        verify(repository).getAllCurrencyCodes()
    }

    @Test
    fun `invoke should return Error when currency table is empty`() = runTest {

        `when`(repository.isCurrencyTableEmpty()).thenReturn(true)
        val result = useCase().toList()

        assert(result[0] is Resource.Loading)
        assert(result[1] is Resource.Error)
        assertEquals((result[1] as Resource.Error).message, "Something went wrong")

        verify(repository).isCurrencyTableEmpty()
        verify(repository, never()).getAllCurrencyCodes()
    }

    @Test
    fun `invoke should return Loading initially`() = runTest {
        `when`(repository.isCurrencyTableEmpty()).thenReturn(false)
        `when`(repository.getAllCurrencyCodes()).thenReturn(listOf("USD", "EUR", "INR"))

        val flow = useCase()
        val result = flow.toList()

        assert(result[0] is Resource.Loading)
    }
}
