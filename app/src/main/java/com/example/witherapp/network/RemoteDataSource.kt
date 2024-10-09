package com.example.witherapp.network

import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.WitherForecastResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteDataSource private constructor(private val apiServices: ApiServices) :
    IRemoteDataSource {

    override suspend fun getWitherOfTheDay(lat: Double, lon: Double, language:String): Flow<CurrentWeatherResponse> = flow {
        val result = apiServices.getWitherOfTheDay(lat, lon,language)
        emit(result)
    }

    override suspend fun getWitherForCast(lat: Double, lon: Double, language:String): Flow<WitherForecastResponse> =
        flow {
            val result = apiServices.getWitherForecast(lat, lon,language)
            emit(result)
        }

    companion object {
        private var instance: RemoteDataSource? = null
         fun getInstance(apiServices: ApiServices): RemoteDataSource {
            return instance ?: synchronized(this) {
                val temp = RemoteDataSource(apiServices)
                instance = temp
                return temp
            }
        }
    }

}