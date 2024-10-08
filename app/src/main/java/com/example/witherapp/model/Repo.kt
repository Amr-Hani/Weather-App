package com.example.witherapp.model

import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.network.RemoteDataSource
import kotlinx.coroutines.flow.Flow

class Repo private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {

    companion object {
        private var instance: Repo? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource
        ): Repo {
            return instance ?: synchronized(this) {
                val temp = Repo(remoteDataSource, localDataSource)
                instance = temp
                temp
            }
        }
    }

    suspend fun getWitherOfTheDay(lat: Double, long: Double,language:String): Flow<CurrentWeatherResponse> =
        remoteDataSource.getWitherOfTheDay(lat, long,language)

    suspend fun getWitherForCast(lat: Double, long: Double,language:String): Flow<WitherForecastResponse> =
        remoteDataSource.getWitherForCast(lat, long,language)



    fun getAllFavoritePlace(): Flow<List<FavoritePlace>> =
        localDataSource.getAllFavoriteProduct()

    suspend fun insertFavoritePlace(favoritePlace: FavoritePlace):Long =
        localDataSource.insert(favoritePlace)

    suspend fun deleteFavoritePlace(favoritePlace: FavoritePlace):Int =
        localDataSource.delete(favoritePlace)


}