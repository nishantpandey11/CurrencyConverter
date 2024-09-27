package com.currency.converter.feature_currency_converter.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Data Access Object (DAO) for accessing and manipulating [Currency] data in the database.
 */
@Dao
interface CurrencyDao {

    /**
     * Inserts or updates a list of [Currency] objects in the database.
     *
     * @param currencies The list of currencies to be upserted.
     */
    @Upsert
    suspend fun upsertCurrencies(currencies: List<Currency>)


    /**
     * Retrieves all [Currency] objects ordered by their code in ascending order.
     *
     * @return A list of all currencies.
     */
    @Query("SELECT * FROM Currency ORDER BY currencyCode ASC")
    suspend fun getAllCurrencies(): List<Currency>

    @Query("SELECT COUNT(*) FROM currency")
    suspend fun getCurrencyCount(): Int

    @Query("SELECT currencyCode FROM currency")
    suspend fun getAllCurrencyCodes(): List<String>


}
