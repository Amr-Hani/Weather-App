package com.example.witherapp.database

import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {

    suspend fun insert(favoritePlace: FavoritePlace): Long
    suspend fun delete(favoritePlace: FavoritePlace): Int
    fun getAllFavoriteProduct(): Flow<List<FavoritePlace>>

    fun getAllAlarmLocation(): Flow<List<SingleAlarm>>
    suspend fun deleteAlarmLocation(singleAlarm: SingleAlarm): Int
    suspend fun insertAlarmLocation(singleAlarm: SingleAlarm): Long
}