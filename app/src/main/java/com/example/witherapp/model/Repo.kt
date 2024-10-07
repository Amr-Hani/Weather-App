package com.example.witherapp.model

import com.example.witherapp.database.FavoritePlaceLocalDataSource
import com.example.witherapp.network.WitherOfTheDayRemoteDataSource
import kotlinx.coroutines.flow.Flow

class Repo private constructor(
    private val witherOfTheDayRemoteDataSource: WitherOfTheDayRemoteDataSource,
    private val favoritePlaceLocalDataSource: FavoritePlaceLocalDataSource
) {

    companion object {
        private var instance: Repo? = null
        fun getInstance(
            witherOfTheDayRemoteDataSource: WitherOfTheDayRemoteDataSource,
            favoritePlaceLocalDataSource: FavoritePlaceLocalDataSource
        ): Repo {
            return instance ?: synchronized(this) {
                val temp = Repo(witherOfTheDayRemoteDataSource, favoritePlaceLocalDataSource)
                instance = temp
                temp
            }
        }
    }

    suspend fun getWitherOfTheDay(lat: Double, long: Double,language:String): Flow<CurrentWeatherResponse> =
        witherOfTheDayRemoteDataSource.getWitherOfTheDay(lat, long,language)

    suspend fun getWitherForCast(lat: Double, long: Double,language:String): Flow<WitherForecastResponse> =
        witherOfTheDayRemoteDataSource.getWitherForCast(lat, long,language)



    fun getAllFavoritePlace(): Flow<List<FavoritePlace>> =
        favoritePlaceLocalDataSource.getAllFavoriteProduct()

    suspend fun insertFavoritePlace(favoritePlace: FavoritePlace):Long =
        favoritePlaceLocalDataSource.insert(favoritePlace)

    suspend fun deleteFavoritePlace(favoritePlace: FavoritePlace):Int =
        favoritePlaceLocalDataSource.delete(favoritePlace)


}