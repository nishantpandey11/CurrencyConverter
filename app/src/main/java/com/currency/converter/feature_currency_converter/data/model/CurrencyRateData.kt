package com.currency.converter.feature_currency_converter.data.model

import com.google.gson.annotations.SerializedName

data class CurrencyRateData(
    @SerializedName("disclaimer") var disclaimer: String? = null,
    @SerializedName("license") var license: String? = null,
    @SerializedName("timestamp") var timestamp: Long = 0L,
    @SerializedName("base") var base: String? = null,
    @SerializedName("rates") var rates: Rates? = Rates()
)
