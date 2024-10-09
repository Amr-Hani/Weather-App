package com.example.witherapp

import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.WitherForecastResponse
import com.example.witherapp.network.IRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRemoteDataSource(
    private val currentWeatherResponse: CurrentWeatherResponse,
    private val witherForecastResponse: WitherForecastResponse
) : IRemoteDataSource {
    override suspend fun getWitherOfTheDay(
        lat: Double,
        lon: Double,
        language: String
    ): Flow<CurrentWeatherResponse> {
        return flowOf(currentWeatherResponse)
    }

    override suspend fun getWitherForCast(
        lat: Double,
        lon: Double,
        language: String
    ): Flow<WitherForecastResponse> {
        return flowOf(witherForecastResponse)
    }
}