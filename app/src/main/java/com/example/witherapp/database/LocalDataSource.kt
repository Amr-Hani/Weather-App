package com.example.witherapp.database

import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

class LocalDataSource(private val dao: DAO) : ILocalDataSource {

    override suspend fun insert(favoritePlace: FavoritePlace): Long =
        dao.insertFavoriteLocation(favoritePlace)

    override suspend fun delete(favoritePlace: FavoritePlace): Int =
        dao.deleteFavoriteLocation(favoritePlace)

    override fun getAllFavoriteProduct(): Flow<List<FavoritePlace>> = dao.getAllMyFavoritePlace()


    override suspend fun insertAlarmLocation(singleAlarm: SingleAlarm): Long =
        dao.insertAlarmLocation(singleAlarm)

    override suspend fun deleteAlarmLocation(singleAlarm: SingleAlarm): Int =
        dao.deleteAlarmLocation(singleAlarm)

    override fun getAllAlarmLocation(): Flow<List<SingleAlarm>> = dao.getAllMyAlarmLocation()
}