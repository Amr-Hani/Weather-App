package com.example.witherapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.witherapp.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavoriteLocation(favoritePlace: FavoritePlace):Long

    @Delete
    suspend fun deleteFavoriteLocation(favoritePlace: FavoritePlace):Int

    @Query("SELECT * FROM my_favorite_place")
    fun getAllMyFavoritePlace():Flow<List<FavoritePlace>>

}