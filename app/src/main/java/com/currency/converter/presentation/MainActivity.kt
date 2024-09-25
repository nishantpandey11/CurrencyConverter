package com.currency.converter.presentation

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
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
import com.currency.converter.utils.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NetworkChangeListener {
    private val currencyViewModel: CurrencyViewModel by viewModels()
    private lateinit var currencySpinner: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnSubmit: Button
    private lateinit var rvCurrencyValue: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ExchangeRateAdapter
    private var exchangeRateList: List<Currency> = emptyList()
    private var selectedCurrencyPosition: Int = 0
    private lateinit var networkReceiver: NetworkChangeReceiver
    private var hasFetchFinished: Boolean = false
    private val TAG = "MainActivity"


    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }
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
        networkReceiver = NetworkChangeReceiver(this)
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
            val amountValueInOtherCurrenciesList =
                currencyViewModel.getCurrencyValue(selectedCurrencyPosition, text, exchangeRateList)
            setAdapter(amountValueInOtherCurrenciesList)
        }
    }

    private fun setAdapter(exchangeRates: List<Currency>) {
        adapter = ExchangeRateAdapter(data = exchangeRates)
        rvCurrencyValue.adapter = adapter
    }

    private fun initObservers() {

        lifecycle.coroutineScope.launch {
            currencyViewModel.getExchangeRate(BuildConfig.APP_ID)

        }

        /*lifecycle.coroutineScope.launch {
            currencyViewModel.getAllCurrencies()

        }*/

        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            currencyViewModel.currencyListState.collect { currencyListState ->
                when (currencyListState) {
                    is CurrencyListState.Loading -> showLoading()
                    is CurrencyListState.Error -> {
                        showError(currencyListState.message)
                        hasFetchFinished = true
                    }

                    is CurrencyListState.ExchangeRateSuccess -> {
                        hideLoading()
                        currencyListState.exchangeRate?.let {
                            exchangeRateList = it
                            setAdapter(it)
                            currencyViewModel.getAllCurrencies()
                        }
                    }

                    is CurrencyListState.Success -> {
                        hideLoading()
                        currencyListState.currencies?.let {
                            initSpinner(it)
                            hasFetchFinished = true
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

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val type = parent?.getItemAtPosition(position).toString()
                selectedCurrencyPosition = position
                AppLogger.e(TAG, type)
            }
        }
    }

    override fun onNetworkChanged() {
        AppLogger.e(TAG, "network changed")
        if (hasFetchFinished && exchangeRateList.isEmpty()) {
            initObservers()
        }
    }
}

interface NetworkChangeListener {
    fun onNetworkChanged()
}