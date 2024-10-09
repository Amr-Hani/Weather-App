package com.example.witherapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


data class CurrentWeatherResponse(
    val coord: Coord,
    val weather: List<WeatherItem>,
    val base: String,
    val main: MainItem,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

data class MainItem(
    val temp: Double,
    val feels_like: Double,
    var temp_min: Double,
    var temp_max: Double,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int,
    val grnd_level: Int
)

data class WeatherItem(val description: String, val icon: String)

data class WitherForecastResponse(val list: List<WitherForecastItem>)

data class WitherForecastItem(
    val main: MainItem,
    val weather: List<WeatherItem>,
    val dt_txt: String
)


data class Coord(
    val lon: Double,
    val lat: Double
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

data class Clouds(
    val all: Int
)

data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

@Entity(tableName = "my_favorite_place")
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "my_alarm_manger")
data class SingleAlarm(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    val address: String,
    var date: String
)