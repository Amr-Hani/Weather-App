package com.example.witherapp.database

import com.example.witherapp.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

class LocalDataSource(val dao: DAO) {

    suspend fun insert(favoritePlace: FavoritePlace): Long =
        dao.insertFavoriteLocation(favoritePlace)

    suspend fun delete(favoritePlace: FavoritePlace): Int =
        dao.deleteFavoriteLocation(favoritePlace)

    fun getAllFavoriteProduct(): Flow<List<FavoritePlace>> = dao.getAllMyFavoritePlace()
}