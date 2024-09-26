package com.currency.converter.feature_currency_converter.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.currency.converter.databinding.CurrencyItemBinding
import com.currency.converter.feature_currency_converter.data.local.Currency

class ExchangeRateAdapter :
    ListAdapter<Currency, ExchangeRateAdapter.ExchangeRateVH>(CurrencyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateVH {
        val binding =
            CurrencyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExchangeRateVH(binding)
    }

    override fun onBindViewHolder(holder: ExchangeRateVH, position: Int) {
        holder.bind(getItem(position))
    }

    class ExchangeRateVH(private val binding: CurrencyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currency: Currency) {
            binding.tvCurrencyName.text = currency.currencyCode
            binding.tvCurrencyValue.text = currency.exchangeRate.toString()
        }
    }
}

class CurrencyDiffCallback : DiffUtil.ItemCallback<Currency>() {
    override fun areItemsTheSame(oldItem: Currency, newItem: Currency): Boolean {
        return oldItem.currencyCode == newItem.currencyCode
    }

    override fun areContentsTheSame(oldItem: Currency, newItem: Currency): Boolean {
        return oldItem == newItem
    }
}