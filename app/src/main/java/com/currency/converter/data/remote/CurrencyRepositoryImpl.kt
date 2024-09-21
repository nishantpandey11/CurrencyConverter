package com.currency.converter.data.remote

import com.currency.converter.data.model.CurrencyRateData
import com.currency.converter.domain.repository.CurrencyRepository
import com.currency.converter.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    CurrencyRepository {
    override suspend fun getCurrencyRates(appID: String): Resource<CurrencyRateData> {
        return try {
            val res = apiService.getCurrencyRates(appID)
            if (res.isSuccessful) {
                res.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Unknown Error Occurred")
            } else
                Resource.Error(res.message())
        } catch (e: HttpException) {
            Resource.Error("Unexpected HttpException " + e.localizedMessage)
        } catch (e: IOException) {
            Resource.Error("IO Exception, couldn't reach server " + e.localizedMessage)
        }

    }


}