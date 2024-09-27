package com.currency.converter.di


import com.currency.converter.feature_currency_converter.data.CurrencyRepositoryImpl
import com.currency.converter.feature_currency_converter.data.local.CurrencyDao
import com.currency.converter.feature_currency_converter.data.local.DataStoreManager
import com.currency.converter.feature_currency_converter.data.remote.ApiService
import com.currency.converter.feature_currency_converter.domain.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides dependencies related to repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides a singleton instance of [CurrencyRepository].
     * */
    @Provides
    @Singleton
    fun provideCurrencyRepository(
        apiService: ApiService, currencyDao: CurrencyDao,
        dataStoreManager: DataStoreManager,
    ): CurrencyRepository =
        CurrencyRepositoryImpl(apiService, currencyDao, dataStoreManager)
}