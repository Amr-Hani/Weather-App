package com.example.witherapp.network

import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.WitherForecastResponse
import kotlinx.coroutines.flow.Flow

interface IRemoteDataSource {
    suspend fun getWitherOfTheDay(
        lat: Double,
        lon: Double,
        language: String
    ): Flow<CurrentWeatherResponse>

    suspend fun getWitherForCast(
        lat: Double,
        lon: Double,
        language: String
    ): Flow<WitherForecastResponse>
}