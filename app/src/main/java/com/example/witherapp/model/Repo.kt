package com.example.witherapp.model

import com.example.witherapp.database.ILocalDataSource
import com.example.witherapp.network.IRemoteDataSource
import kotlinx.coroutines.flow.Flow

class Repo (
    private val iRemoteDataSource: IRemoteDataSource,
    private val iLocalDataSource: ILocalDataSource
) : IRepo {

    companion object {
        private var instance: Repo? = null
        fun getInstance(
            iRemoteDataSource: IRemoteDataSource,
            iLocalDataSource: ILocalDataSource
        ): Repo {
            return instance ?: synchronized(this) {
                val temp = Repo(iRemoteDataSource, iLocalDataSource)
                instance = temp
                temp
            }
        }
    }

    override suspend fun getWitherOfTheDay(lat: Double, long: Double, language:String): Flow<CurrentWeatherResponse> =
        iRemoteDataSource.getWitherOfTheDay(lat, long,language)

    override suspend fun getWitherForCast(lat: Double, long: Double, language:String): Flow<WitherForecastResponse> =
        iRemoteDataSource.getWitherForCast(lat, long,language)



    override fun getAllFavoritePlace(): Flow<List<FavoritePlace>> =
        iLocalDataSource.getAllFavoriteProduct()

    override suspend fun insertFavoritePlace(favoritePlace: FavoritePlace):Long =
        iLocalDataSource.insert(favoritePlace)

    override suspend fun deleteFavoritePlace(favoritePlace: FavoritePlace):Int =
        iLocalDataSource.delete(favoritePlace)


    override fun getAllAlarmLocation(): Flow<List<SingleAlarm>> =
        iLocalDataSource.getAllAlarmLocation()

    override suspend fun insertAlarmLocation(singleAlarm: SingleAlarm):Long =
        iLocalDataSource.insertAlarmLocation(singleAlarm)

    override suspend fun deleteAlarmLocation(singleAlarm: SingleAlarm):Int =
        iLocalDataSource.deleteAlarmLocation(singleAlarm)


}