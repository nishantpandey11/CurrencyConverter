package com.currency.converter.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.currency.converter.BuildConfig
import com.currency.converter.R
import com.currency.converter.data.local.Currency
import com.currency.converter.presentation.model.CurrencyListState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val currencyViewModel: CurrencyViewModel by viewModels()
    private lateinit var currencySpinner: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnSubmit: Button
    private lateinit var rvCurrencyValue: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ExchangeRateAdapter
    private lateinit var exchangeRateList: List<Currency>
    private var selectedCurrencyPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        initObservers()


    }

    private fun initViews() {
        currencySpinner = findViewById(R.id.spinner_currency)
        etAmount = findViewById(R.id.etAmount)
        rvCurrencyValue = findViewById(R.id.rv_currency_value)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.pb)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvCurrencyValue.layoutManager = layoutManager

        btnSubmit.setOnClickListener {
            val text = etAmount.text.toString()
            if(text.isEmpty()){
                showError("Please enter a valid amount")
                return@setOnClickListener
            }
            val amount = text.toDouble()
            setAdapter(amount, exchangeRateList)
            //adapter.refreshCurrencyValue(etAmount.text.toString().toDouble())
        }
    }

    private fun setAdapter(amount: Double, exchangeRates: List<Currency>) {
        adapter = ExchangeRateAdapter(selectedCurrencyPosition, amount, exchangeRates)
        rvCurrencyValue.adapter = adapter
    }

    private fun initObservers() {
        lifecycle.coroutineScope.launch {
            currencyViewModel.getExchangeRate(BuildConfig.APP_ID)

        }

        lifecycle.coroutineScope.launch {
            currencyViewModel.getAllCurrencies()
        }

        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            currencyViewModel.currencyListState.collect { currencyListState ->
                when (currencyListState) {
                    is CurrencyListState.Loading -> showLoading()
                    is CurrencyListState.Error -> showError(currencyListState.message)

                    is CurrencyListState.Success -> {
                        hideLoading()
                        currencyListState.currencies?.let { initSpinner(it) }
                    }

                    is CurrencyListState.ExchangeRateSuccess -> {
                        hideLoading()
                        currencyListState.exchangeRate?.let {
                            exchangeRateList = it
                            setAdapter(1.0, it)
                        }
                    }
                }
            }
        }


    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }


    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun initSpinner(currencyList: List<String>) {

        currencySpinner.adapter = ArrayAdapter(this, R.layout.spinner_item, currencyList)

        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("===>", "onNothingSelected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val type = parent?.getItemAtPosition(position).toString()
                selectedCurrencyPosition = position
                Log.e("===>", type)
            }
        }
    }
}