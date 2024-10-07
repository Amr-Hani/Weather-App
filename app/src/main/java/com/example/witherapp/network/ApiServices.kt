package com.example.witherapp.network

import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.WitherForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "25c8719c2b0393dd86decde1af7edfdb"

interface ApiServices {
    @GET("weather")
    suspend fun getWitherOfTheDay(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") lang: String = "en",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): CurrentWeatherResponse

    @GET("forecast")
    suspend fun getWitherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") lang: String = "en",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = API_KEY
    ): WitherForecastResponse


}