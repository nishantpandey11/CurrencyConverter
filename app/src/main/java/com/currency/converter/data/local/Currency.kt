package com.currency.converter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
    @PrimaryKey
    val currencyCode: String,
    val exchangeRate: Double
)