package com.example.witherapp

import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.FavoritePlace
import com.example.witherapp.model.IRepo
import com.example.witherapp.model.WitherForecastResponse
import kotlinx.coroutines.flow.Flow

class FakeRepo():IRepo {
    override suspend fun getWitherOfTheDay(
        lat: Double,
        long: Double,
        language: String
    ): Flow<CurrentWeatherResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getWitherForCast(
        lat: Double,
        long: Double,
        language: String
    ): Flow<WitherForecastResponse> {
        TODO("Not yet implemented")
    }

    override fun getAllFavoritePlace(): Flow<List<FavoritePlace>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertFavoritePlace(favoritePlace: FavoritePlace): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavoritePlace(favoritePlace: FavoritePlace): Int {
        TODO("Not yet implemented")
    }

    override fun getAllAlarmLocation(): Flow<List<SingleAlarm>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlarmLocation(favoritePlace: SingleAlarm): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlarmLocation(favoritePlace: SingleAlarm): Int {
        TODO("Not yet implemented")
    }
}