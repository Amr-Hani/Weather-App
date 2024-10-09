package com.example.witherapp.model

import kotlinx.coroutines.flow.Flow

interface IRepo {
    suspend fun getWitherOfTheDay(
        lat: Double,
        long: Double,
        language: String
    ): Flow<CurrentWeatherResponse>

    suspend fun getWitherForCast(
        lat: Double,
        long: Double,
        language: String
    ): Flow<WitherForecastResponse>

    fun getAllFavoritePlace(): Flow<List<FavoritePlace>>
    suspend fun insertFavoritePlace(favoritePlace: FavoritePlace): Long
    suspend fun deleteFavoritePlace(favoritePlace: FavoritePlace): Int

    fun getAllAlarmLocation(): Flow<List<SingleAlarm>>
    suspend fun insertAlarmLocation(favoritePlace: SingleAlarm):Long
    suspend fun deleteAlarmLocation(favoritePlace: SingleAlarm):Int
}