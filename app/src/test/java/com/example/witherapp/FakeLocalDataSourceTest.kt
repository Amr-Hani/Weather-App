package com.example.witherapp

import com.example.witherapp.database.ILocalDataSource
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.FavoritePlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalDataSourceTest(private val localDataSourceList:MutableList<FavoritePlace> = mutableListOf() ) :ILocalDataSource {
    override suspend fun insert(favoritePlace: FavoritePlace): Long {
        return if (localDataSourceList.add(favoritePlace)) 1 else 0
    }

    override suspend fun delete(favoritePlace: FavoritePlace): Int {
        return if (localDataSourceList.remove(favoritePlace)) 1 else 0
    }

    override fun getAllFavoriteProduct(): Flow<List<FavoritePlace>> {
        return flowOf(localDataSourceList)
    }

    override fun getAllAlarmLocation(): Flow<List<SingleAlarm>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlarmLocation(singleAlarm: SingleAlarm): Int {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlarmLocation(singleAlarm: SingleAlarm): Long {
        TODO("Not yet implemented")
    }
}