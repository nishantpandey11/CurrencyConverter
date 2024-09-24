package com.currency.converter.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.currency.converter.R
import com.currency.converter.data.local.Currency

class ExchangeRateAdapter(
    private val selectedCurrency: Int,
    private var amount: Double = 1.0,
    val data: List<Currency>
) :
    RecyclerView.Adapter<ExchangeRateAdapter.ExchangeRateVH>() {
    class ExchangeRateVH(view: View) : ViewHolder(view) {
        val tvCurrencyName: TextView = view.findViewById(R.id.tv_currency_name)
        val tvCurrencyValue: TextView = view.findViewById(R.id.tv_currency_value)
    }

    fun refreshCurrencyValue(amount: Double) {
        this.amount = amount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExchangeRateVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.currency_item, parent, false)
        return ExchangeRateVH(view)
    }

    override fun onBindViewHolder(holder: ExchangeRateVH, position: Int) {

        //selected exchange rate would be selected currency exchange rate
        //val amountInUsd = amount / selectedExchangeRateToUsd
        val amountInUsd = amount / data[selectedCurrency].exchangeRate
        val currValue = amountInUsd * data[position].exchangeRate

        holder.tvCurrencyName.text = data[position].currencyCode
        holder.tvCurrencyValue.text = String.format("%.3f", currValue)

    }

    override fun getItemCount(): Int = data.size
}