package com.currency.converter.feature_currency_converter.presentation.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.currency.converter.BuildConfig
import com.currency.converter.R
import com.currency.converter.databinding.ActivityMainBinding
import com.currency.converter.feature_currency_converter.data.local.Currency
import com.currency.converter.feature_currency_converter.presentation.viewmodel.CurrencyViewModel
import com.currency.converter.utils.AppLogger
import com.currency.converter.utils.NetworkChangeListener
import com.currency.converter.utils.NetworkChangeReceiver
import com.currency.converter.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NetworkChangeListener {
    private lateinit var binding: ActivityMainBinding
    private val currencyViewModel: CurrencyViewModel by viewModels()
    private lateinit var adapter: ExchangeRateAdapter
    private lateinit var networkReceiver: NetworkChangeReceiver
    private var exchangeRateList: List<Currency> = emptyList()
    private var selectedCurrencyPosition: Int = 0
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initRecyclerView()
        initListeners()
        getExchangeData()
        initObservers()
    }

    private fun initRecyclerView() {
        adapter = ExchangeRateAdapter()
        binding.rvCurrencyValue.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun initListeners() {
        networkReceiver = NetworkChangeReceiver(this)
        binding.btnSubmit.setOnClickListener {
            val amount = binding.etAmount.text.toString()
            currencyViewModel.onSubmitClick(amount, exchangeRateList, selectedCurrencyPosition)
        }
    }

    private fun updateExchangeList(exchangeList: List<Currency>) {
        adapter.submitList(exchangeList)
    }

    private fun getExchangeData() {
        lifecycle.coroutineScope.launch {
            currencyViewModel.getExchangeRate(BuildConfig.APP_ID)
        }
    }

    private fun initObservers() {

        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            currencyViewModel.exchangeRateState.collect { currencyListState ->
                when (currencyListState) {
                    is Resource.Loading -> showLoading()
                    is Resource.Error -> {
                        updateExchangeList(emptyList())
                        showError(currencyListState.message!!)
                        hasFetchFinished = true
                    }

                    is Resource.Success -> {
                        hideLoading()
                        currencyListState.data?.let {
                            exchangeRateList = it
                            updateExchangeList(exchangeRateList)
                            currencyViewModel.getAllCurrencies()
                        }
                    }
                }
            }
        }

        lifecycle.coroutineScope.launch(Dispatchers.Main) {
            currencyViewModel.currencyListState.collect { currencyListState ->
                when (currencyListState) {
                    is Resource.Loading -> showLoading()
                    is Resource.Error -> {
                        showError(currencyListState.message!!)
                        hasFetchFinished = true
                    }

                    is Resource.Success -> {
                        hideLoading()
                        currencyListState.data?.let {
                            initSpinner(it)
                            hasFetchFinished = true
                        }
                    }
                }

            }
        }

        currencyViewModel.currencyValueState.observe(this) {
            updateExchangeList(it)
        }


        currencyViewModel.validationState.observe(this) {
            val error = getString(it)
            if (error.isNotEmpty())
                showError(error)
        }

    }

    private fun showLoading() {
        binding.pb.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.pb.visibility = View.GONE
    }


    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun initSpinner(currencyList: List<String>) {

        binding.spinnerCurrency.adapter = ArrayAdapter(this, R.layout.spinner_item, currencyList)

        binding.spinnerCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
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
        AppLogger.e(TAG, "Device Online")
        if (hasFetchFinished && exchangeRateList.isEmpty()) {
            getExchangeData()
            AppLogger.e(TAG, "Device Online called")
        }
    }
}

