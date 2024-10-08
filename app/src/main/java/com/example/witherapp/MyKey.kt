package com.example.witherapp

object MyKey {
    const val MY_SHARED_PREFERENCES="MySharedPreferences"
    const val LANGUAGE_KEY="LANGUAGE"
    const val LOCATION_KEY = "LOCATION"
    const val WIND_KEY = "WIND"
    const val UNIT_KEY = "UNIT"
    const val NOTIFICATION_KEY = "NOTIFICATION"
    const val CACHING_CURRENT_ARABIC = "CURRENT_ARABIC"
    const val CACHING_DAILY_HOUR_ARABIC = "DAILY_HOUR_ARABIC"
    const val CACHING_WITHER_FOR_CAST_ARABIC = "WITHER_FOR_CAST_ARABIC"
    const val CACHING_CURRENT_ENGLISH = "CURRENT_ENGLISH"
    const val CACHING_DAILY_HOUR_ENGLISH = "DAILY_HOUR_ENGLISH"
    const val CACHING_WITHER_FOR_CAST_ENGLISH = "WITHER_FOR_CAST_ENGLISH"

    fun getImage(icon: String): Int {
        val iconValue: Int
        when (icon) {
            "01d" -> iconValue = R.drawable.clear
            "01n" -> iconValue = R.drawable.clear
            "02d" -> iconValue = R.drawable.cloudy
            "02n" -> iconValue = R.drawable.cloudy
            "03n" -> iconValue = R.drawable.cloudy
            "03d" -> iconValue = R.drawable.cloudy
            "04d" -> iconValue = R.drawable.cloudy
            "04n" -> iconValue = R.drawable.cloudy
            "09d" -> iconValue = R.drawable.rain
            "09n" -> iconValue = R.drawable.rain
            "10d" -> iconValue = R.drawable.rain
            "10n" -> iconValue = R.drawable.rain
            "11d" -> iconValue = R.drawable.storm
            "11n" -> iconValue = R.drawable.storm
            "13d" -> iconValue = R.drawable.snow
            "13n" -> iconValue = R.drawable.snow
            "50d" -> iconValue = R.drawable.mist
            "50n" -> iconValue = R.drawable.mist
            else -> iconValue = R.drawable.clear
        }
        return iconValue
        }
}
enum class Language{ar,en}
enum class Units{celsius,fehrenheit,kelvin}
enum class Location{gps,map}
enum class Wind{meter,mile}
enum class Notification{enabled,disabled}
