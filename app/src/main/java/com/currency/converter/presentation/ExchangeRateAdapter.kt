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
    private var data: List<Currency>
) :
    RecyclerView.Adapter<ExchangeRateAdapter.ExchangeRateVH>() {
    class ExchangeRateVH(view: View) : ViewHolder(view) {
        val tvCurrencyName: TextView = view.findViewById(R.id.tv_currency_name)
        val tvCurrencyValue: TextView = view.findViewById(R.id.tv_currency_value)
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
        holder.tvCurrencyName.text = data[position].currencyCode
        holder.tvCurrencyValue.text = data[position].exchangeRate.toString()

    }

    override fun getItemCount(): Int = data.size
}