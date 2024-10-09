package com.example.witherapp.model

import com.example.witherapp.FakeLocalDataSourceTest
import com.example.witherapp.FakeRemoteDataSource
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test


class RepoTest {
    lateinit var fakeLocalDataSourceTest: FakeLocalDataSourceTest
    lateinit var fakeRemoteDataSource: FakeRemoteDataSource
    lateinit var repo: Repo
    val currentWeather = CurrentWeatherResponse(
        id = 501,
        coord = Coord(10.99, 44.34),
        weather = listOf(WeatherItem(description = "moderate rain", "10d")),
        base = "stations",
        main = MainItem(285.95, 285.74, 284.94, 287.76, 1009, 94, 1009, 942),
        visibility = 8412,
        wind = Wind(2.87, 167, 5.81),
        clouds = Clouds(100),
        dt = 1728380785,
        sys = Sys(2, 2044440, "IT", 1728364943, 1728405860),
        timezone = 7200,
        name = "Zocca",
        cod = 200
    )

    var weatherForCastResponse = WitherForecastResponse(

        list = listOf(
            WitherForecastItem(
                main = MainItem(
                    temp = 20.5,
                    feels_like = 19.8,
                    temp_min = 18.0,
                    temp_max = 23.0,
                    pressure = 1012,
                    humidity = 85,
                    sea_level = 1012,
                    grnd_level = 1000
                ),
                weather = listOf(
                    WeatherItem(

                        description = "clear sky",
                        icon = "01d"
                    )
                ),
                dt_txt = "2024-10-08 12:00:00"
            )
        )
    )

    @Before
    fun setUp() {
        fakeLocalDataSourceTest = FakeLocalDataSourceTest()
        fakeRemoteDataSource = FakeRemoteDataSource(currentWeather, weatherForCastResponse)

        repo = Repo.getInstance(fakeRemoteDataSource, fakeLocalDataSourceTest)
    }

    @Test
    fun getCurrentWitherOfDay_LongAndLat_CurrentDayWeatherResponse() = runBlocking {
        var result = repo.getWitherOfTheDay(0.0, 0.0, "en").first()
        assertEquals(currentWeather, result)
    }

    @Test
    fun InsertFavoriteLocation_FavouriteLocationItem_equalsTrueOrFalse() = runBlocking {
        val favoritePlace = FavoritePlace(
            primaryKey = 500,
            address = "Sharkia",
            latitude = 10.111111,
            longitude = -100.55

        )
        val result = repo.insertFavoritePlace(favoritePlace)

        val allFavoritesPlace = repo.getAllFavoritePlace().first()

        assertThat(allFavoritesPlace.contains(favoritePlace), `is`(true))

        assertEquals(result, 1)
    }

    @Test
    fun deleteFavoriteLocation_FavouriteLocationItem_equalsTrueOrFalse() = runBlocking {
        val favoritePlace = FavoritePlace(
            primaryKey = 500,
            address = "Sharkia",
            latitude = 10.111111,
            longitude = -100.55

        )
        val result = repo.deleteFavoritePlace(favoritePlace)
        assertEquals(result, 0)
    }
}