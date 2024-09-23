package com.currency.converter.di


import com.currency.converter.data.local.CurrencyDao
import com.currency.converter.data.local.DataStoreManager
import com.currency.converter.data.remote.ApiService
import com.currency.converter.data.CurrencyRepositoryImpl
import com.currency.converter.domain.CurrencyRepository
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
    @Provides
    @Singleton
    fun provideCurrencyRepository(
        apiService: ApiService, currencyDao: CurrencyDao,
        dataStoreManager: DataStoreManager,
    ): CurrencyRepository =
        CurrencyRepositoryImpl(apiService, currencyDao, dataStoreManager)
}