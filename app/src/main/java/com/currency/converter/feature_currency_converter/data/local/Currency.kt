package com.currency.converter.feature_currency_converter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
    @PrimaryKey
    val currencyCode: String,
    val exchangeRate: String
)