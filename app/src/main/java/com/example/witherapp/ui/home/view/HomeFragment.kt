package com.example.witherapp.ui.home.view

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.witherapp.ApiState
import com.example.witherapp.Language
import com.example.witherapp.MyKey
import com.example.witherapp.Units
import com.example.witherapp.Wind
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentHomeBinding
import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.Repo
import com.example.witherapp.model.WitherForecastItem
import com.example.witherapp.model.WitherForecastResponse
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.ui.home.viewmodel.HomeViewModel
import com.example.witherapp.ui.home.viewmodel.HomeViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val MY_LOCATION_PERMISSION = 1999
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var homeViewModel: HomeViewModel
    lateinit var homeViewModelFactory: HomeViewModelFactory

    lateinit var binding: FragmentHomeBinding

    lateinit var dailyHourAdapter: DailyHourAdapter
    lateinit var witherForCastAdapter: WitherForCastAdapter

    lateinit var sharedPreferences: SharedPreferences
    lateinit var language: String
    lateinit var unit: String
    lateinit var wind: String

    var max = Double.MIN_VALUE
    var min = Double.MAX_VALUE

    val gson = Gson()

    companion object {
        var isConnected = false
    }

    var message: String? = null

    override fun onStart() {
        super.onStart()

        val argument: MutableList<String> = MutableList(3) { "" }
        if (requireArguments() != null) {
            val split = HomeFragmentArgs.fromBundle(requireArguments()).latLong.split(",")
            if (split.size == 3) {
                argument[0] = split[0]
                argument[1] = split[1]
                argument[2] = split[2]
            } else {
                Log.e("TAG", "Invalid latLong format, expected 3 parts but got ${split.size}")
            }
        }
        val message = argument[0]
        val latitude = argument[1]
        val longitude = argument[2]

        Log.d("TAG", "onStart: argument = $argument")
        Log.d("TAG", "onStart: argument = $isConnected")
        if (isConnected) {
            when (message) {
                "FAVORITE" -> {
                    getFavoriteLocation(latitude.toDouble(), longitude.toDouble())
                    Log.d("TAG", "onStart: message FAVORITE")
                }

                "MAP" -> {
                    getFavoriteLocation(latitude.toDouble(), longitude.toDouble())
                }

                else -> {

                    if (checkSelfPermission()) {
                        Log.d("TAG", "onStart: checkSelfPermission")
                        if (isLocationEnabled()) {
                            getFreshLocation()
                        } else {
                            enableLocationServices()
                        }
                    } else {
                        Log.d("TAG", "onStart: requestPermission")
                        requestPermission()
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModelFactory = HomeViewModelFactory(
            Repo.getInstance(
                RemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                LocalDataSource(
                    MyRoomDatabase.getInstance(requireContext()).getAllFavoritePlace()
                )
            )
        )
        homeViewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
        dailyHourAdapter = DailyHourAdapter()
        witherForCastAdapter = WitherForCastAdapter()

        binding.rvTempretureDay.apply {
            adapter = dailyHourAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvWitherForCast.apply {
            adapter = witherForCastAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        sharedPreferences =
            requireContext().getSharedPreferences(MyKey.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        language = sharedPreferences.getString(MyKey.LANGUAGE_KEY, Language.en.toString())
            ?: Language.en.toString()
        unit = sharedPreferences.getString(MyKey.UNIT_KEY, Units.celsius.toString())
            ?: Units.celsius.toString()
        wind = sharedPreferences.getString(MyKey.WIND_KEY, Wind.meter.toString())
            ?: Wind.meter.toString()

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        isConnected =
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        Log.d("TAG", "onViewCreated: isConnected $isConnected")
        if (!isConnected) {
            binding.progressBar.visibility = View.GONE
            showFromCaching()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            currentWitherOfTheDayLifeCycle()
            forCastWitherOfTheWeekLifeCycle()
        }
    }

    fun currentWitherOfTheDayLifeCycle() {
        lifecycleScope.launch {
            homeViewModel.witherOfTheDayStateFlow.collectLatest {
                when (it) {
                    is ApiState.Loading -> {
                        binding.cardView.visibility = View.GONE
                        binding.cardView2.visibility = View.GONE
                        binding.rvTempretureDay.visibility = View.GONE
                        binding.rvWitherForCast.visibility = View.GONE
                        Thread.sleep(1000)
                    }

                    is ApiState.Success -> {
                        currentWitherOfTheDaySuccess(it.data, language)
                        binding.progressBar.visibility = View.GONE
                        binding.cardView.visibility = View.VISIBLE
                        binding.cardView2.visibility = View.VISIBLE
                        binding.rvTempretureDay.visibility = View.VISIBLE
                        binding.rvWitherForCast.visibility = View.VISIBLE
                    }

                    else -> {
                        Log.d("TAG", "onCreateView: Fail witherOfTheDay ${it}")
                    }
                }
            }
        }
    }

    fun currentWitherOfTheDaySuccess(currentDay: CurrentWeatherResponse, language: String) {
        when (language) {
            "ar" -> {
                when (unit) {
                    "celsius" -> {
                        binding.tvTempreture.text = "${currentDay.main.temp} س "
                        binding.tvFeelsLike.text = "${currentDay.main.feels_like} س "
                    }

                    "kelvin" -> {
                        binding.tvTempreture.text =
                            "${convertFromCelsiusToKelvin(currentDay.main.temp)} ك "
                        binding.tvFeelsLike.text =
                            "${convertFromCelsiusToKelvin(currentDay.main.feels_like)} ك "
                    }

                    "fehrenheit" -> {
                        binding.tvTempreture.text =
                            "${convertFromCelsiusToFahrenheit(currentDay.main.temp)}ف "
                        binding.tvFeelsLike.text =
                            "${convertFromCelsiusToFahrenheit(currentDay.main.feels_like)}ف "
                    }
                }
                when (wind) {
                    "mile" -> {
                        binding.tvWindValue.text =
                            "${convertMeterPerSecToMilePerHour(currentDay.wind.speed)}ميل/س "
                    }

                    else -> {
                        binding.tvWindValue.text = "${currentDay.wind.speed}م/ث "
                    }
                }
                binding.tvDescription.text = currentDay.weather.get(0).description
                Log.d("TAG", "onCreateView: Success witherOfTheDay arabic")
                binding.tvPressureValue.text = "${currentDay.main.pressure}ض.ج "
                binding.tvHumidityValue.text = "${currentDay.main.humidity} % "

                binding.tvCloudValue.text = "${currentDay.clouds.all} %"
                binding.tvVisibilityValue.text = "${currentDay.visibility} م "
                binding.tvLocation.text =
                    getLocationName(currentDay.coord.lat, currentDay.coord.lon)
                binding.imageView2.setImageResource(MyKey.getImage(currentDay.weather.get(0).icon))
                val jsonCurrentDay = gson.toJson(currentDay)
                sharedPreferences.edit().putString(MyKey.CACHING_CURRENT_ARABIC, jsonCurrentDay)
                    .apply()
            }

            "en" -> {
                when (unit) {
                    "celsius" -> {
                        binding.tvTempreture.text = "${currentDay.main.temp} °C"
                        binding.tvFeelsLike.text = "${currentDay.main.feels_like} °C"
                    }

                    "kelvin" -> {
                        binding.tvTempreture.text =
                            "${convertFromCelsiusToKelvin(currentDay.main.temp)} K"
                        binding.tvFeelsLike.text =
                            "${convertFromCelsiusToKelvin(currentDay.main.feels_like)} K"
                        binding.tvLocation.text =
                            getLocationName(currentDay.coord.lat, currentDay.coord.lon)
                    }

                    "fehrenheit" -> {
                        binding.tvTempreture.text =
                            "${convertFromCelsiusToFahrenheit(currentDay.main.temp)} F"
                        binding.tvFeelsLike.text =
                            "${convertFromCelsiusToFahrenheit(currentDay.main.feels_like)} F"
                    }
                }
                when (wind) {
                    "mile" -> {
                        binding.tvWindValue.text =
                            "${convertMeterPerSecToMilePerHour(currentDay.wind.speed)} Mile/Hr"
                    }

                    else -> {
                        binding.tvWindValue.text = "${currentDay.wind.speed} M/Sec"
                    }
                }
                binding.tvDescription.text = currentDay.weather.get(0).description
                Log.d("TAG", "onCreateView: Success witherOfTheDay english")
                binding.tvPressureValue.text = "${currentDay.main.pressure} hpa"
                binding.tvHumidityValue.text = "${currentDay.main.humidity} %"
                binding.tvCloudValue.text = "${currentDay.clouds.all} %"
                binding.tvLocation.text =
                    getLocationName(currentDay.coord.lat, currentDay.coord.lon)
                binding.tvVisibilityValue.text = "${currentDay.visibility} m"

                binding.imageView2.setImageResource(MyKey.getImage(currentDay.weather.get(0).icon))
                val jsonCurrentDay = gson.toJson(currentDay)
                sharedPreferences.edit().putString(MyKey.CACHING_CURRENT_ENGLISH, jsonCurrentDay)
                    .apply()
            }
        }

    }

    fun forCastWitherOfTheWeekLifeCycle() {
        lifecycleScope.launch {
            homeViewModel.witherForCastStateFlow.collect {
                when (it) {
                    is ApiState.Failure -> {
                    }

                    is ApiState.Loading -> Log.d("TAG", "onCreateView: Success witherForCast")
                    is ApiState.Success -> {
                        forCastHourlyOfTheDay(it.data)
                        forCastDayOfTheWeek(it.data)
                    }
                }
            }
        }
    }

    fun forCastHourlyOfTheDay(witherForecast: WitherForecastResponse) {
        val currentDay = witherForecast.list.get(0).dt_txt.split(" ")
        Log.d("TAG", "onCreateView: ${currentDay.get(0)}")
        var currentDayDateArr = currentDay.get(0).split("-").toMutableList()
        if (currentDayDateArr.get(2).toInt() < 10) {
            var tomorrowDay = currentDayDateArr.get(2).toInt() + 1
            currentDayDateArr[2] = "0" + tomorrowDay.toString()
        } else {
            var tomorrowDay = currentDayDateArr.get(2).toInt() + 1
            currentDayDateArr[2] = tomorrowDay.toString()
        }
        val tomorrowsDate = String.format(
            "%04d-%02d-%02d",
            currentDayDateArr[0].toInt(),
            currentDayDateArr[1].toInt(),
            currentDayDateArr[2].toInt()
        )
        val mutwitherForCatMutableList = mutableListOf<WitherForecastItem>()
        for (wither in witherForecast.list) {
            if (wither.dt_txt.startsWith(currentDay.get(0)) || wither.dt_txt.startsWith(
                    tomorrowsDate
                )
            ) {
                mutwitherForCatMutableList.add(wither)
            }
        }
        dailyHourAdapter.submitList(mutwitherForCatMutableList)
        val jsonForCast = gson.toJson(mutwitherForCatMutableList)
        sharedPreferences.edit().putString(MyKey.CACHING_DAILY_HOUR_ENGLISH, jsonForCast).apply()
        sharedPreferences.edit().putString(MyKey.CACHING_DAILY_HOUR_ARABIC, jsonForCast).apply()
    }

    fun forCastDayOfTheWeek(witherForecast: WitherForecastResponse) {
        binding.tvDate.text = witherForecast.list.get(0).dt_txt.split(" ")[0]

        val uniqueDates = mutableSetOf<String>()
        var uniqueDays = mutableListOf<WitherForecastItem>()

        witherForecast.list.forEach { item ->
            val date = item.dt_txt.split(" ")[0]
            if (uniqueDates.add(date)) {
                uniqueDays.add(item)
                max = Double.MIN_VALUE
                min = Double.MAX_VALUE
            }
            if (item.main.temp_max > max) max = item.main.temp_max
            if (item.main.temp_min < min) min = item.main.temp_min
            uniqueDays.get(uniqueDays.size - 1).main.temp_max = max
            uniqueDays.get(uniqueDays.size - 1).main.temp_min = min
        }
        witherForCastAdapter.submitList(uniqueDays)
        val jsonForCast = gson.toJson(uniqueDays)
        sharedPreferences.edit().putString(MyKey.CACHING_WITHER_FOR_CAST_ENGLISH, jsonForCast)
            .apply()
        sharedPreferences.edit().putString(MyKey.CACHING_WITHER_FOR_CAST_ARABIC, jsonForCast)
            .apply()
    }

    fun getFavoriteLocation(latitude: Double, longtude: Double) {
        homeViewModel.getWitherOfTheDay(latitude, longtude, language)
        homeViewModel.getWitherForCast(latitude, longtude, language)
    }

    fun getLocationName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext())
        val readableLocation = geocoder.getFromLocation(latitude, longitude, 1)
        val address = readableLocation?.get(0)

        val city = address?.subLocality ?: address?.locality ?: "Unknown City"
        val state = address?.adminArea ?: "Unknown State"
        val country = address?.countryName ?: "Unknown Country"

        return "$city, $state ,$country"
    }

    fun showFromCaching() {
        when (message) {
            "FAVORITE" -> {
                Toast.makeText(
                    requireContext(),
                    "No NetWork",
                    Toast.LENGTH_SHORT
                ).show()
            }

            "MAP" -> {
                Toast.makeText(
                    requireContext(),
                    "No NetWork",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                when (language) {
                    "ar" -> {
                        val cachingCurrent = gson.fromJson(
                            sharedPreferences.getString(
                                MyKey.CACHING_CURRENT_ARABIC,
                                "Un Known Data"
                            )
                                ?: "Un Known Data",
                            CurrentWeatherResponse::class.java
                        )
                        val type = object : TypeToken<List<WitherForecastItem>>() {}.type
                        val cachingDailyHour: List<WitherForecastItem> = gson.fromJson(
                            sharedPreferences.getString(
                                MyKey.CACHING_DAILY_HOUR_ARABIC,
                                "Un Known Data"
                            ) ?: "Un Known Data",
                            type
                        )
                        val cachingWitherForCast: List<WitherForecastItem> = gson.fromJson(
                            sharedPreferences.getString(MyKey.CACHING_WITHER_FOR_CAST_ARABIC, "Un Known Data") ?: "Un Known Data",
                            type
                        )
                        if (sharedPreferences.getString(
                                MyKey.CACHING_CURRENT_ARABIC,
                                "Un Known Data"
                            ) == "Un Known Data"
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "مفيش انترنت ومفيش داتا قديمه",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showCachingCurrentDay(cachingCurrent, language)
                            showCachingDailyHour(WitherForecastResponse(cachingDailyHour))
                            showCachingWitherForCast(WitherForecastResponse(cachingWitherForCast))
                        }
                        Toast.makeText(
                            requireContext(),
                            "مفيش انترنت ودى داتا قديمه",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        val cachingCurrent = gson.fromJson(
                            sharedPreferences.getString(
                                MyKey.CACHING_CURRENT_ENGLISH,
                                "Un Known Data"
                            )
                                ?: "Un Known Data",
                            CurrentWeatherResponse::class.java
                        )
                        val type = object : TypeToken<List<WitherForecastItem>>() {}.type
                        val cachingDailyHour: List<WitherForecastItem> = gson.fromJson(
                            sharedPreferences.getString(
                                MyKey.CACHING_DAILY_HOUR_ENGLISH,
                                "Un Known Data"
                            ) ?: "Un Known Data",
                            type
                        )
                        val cachingWitherForCast: List<WitherForecastItem> = gson.fromJson(
                            sharedPreferences.getString(
                                MyKey.CACHING_WITHER_FOR_CAST_ENGLISH,
                                "Un Known Data"
                            ) ?: "Un Known Data",
                            type
                        )
                        if (sharedPreferences.getString(
                                MyKey.CACHING_CURRENT_ARABIC,
                                "Un Known Data"
                            ) == "Un Known Data"
                        ) {
                            Toast.makeText(
                                requireContext(),
                                "No Internet And Un Known Data In Caching",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showCachingCurrentDay(cachingCurrent, language)
                            showCachingDailyHour(WitherForecastResponse(cachingDailyHour))
                            showCachingWitherForCast(WitherForecastResponse(cachingWitherForCast))
                        }
                        Toast.makeText(
                            requireContext(),
                            "No Internet And Get Data From Caching Caching",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        Log.d("TAG", "onStart: get data from caching")

    }

    fun convertFromCelsiusToFahrenheit(temp: Double): Int {
        return ((temp * 1.8) + 32).toInt()
    }

    fun convertFromCelsiusToKelvin(temp: Double): Int {
        return (temp + 273.15).toInt()
    }

    fun convertMeterPerSecToMilePerHour(m: Double): Int {
        return (m * 2.23694).toInt()
    }

    fun showCachingCurrentDay(currentDay: CurrentWeatherResponse, language: String) {
        currentWitherOfTheDaySuccess(currentDay, language)
    }

    fun showCachingDailyHour(witherForecast: WitherForecastResponse) {
        dailyHourAdapter.submitList(witherForecast.list)
    }

    fun showCachingWitherForCast(witherForecast: WitherForecastResponse) {
        witherForCastAdapter.submitList(witherForecast.list)
    }

    //Location
    @SuppressLint("MissingPermission")
    fun getFreshLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(LocationRequest.Builder(20000).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build(), object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val longitude = p0.locations.lastOrNull()?.longitude
                val latitude = p0.locations.lastOrNull()?.latitude

                if (latitude != null && longitude != null) {
                    homeViewModel.getWitherOfTheDay(latitude, longitude, language)
                    homeViewModel.getWitherForCast(latitude, longitude, language)
                }
            }
        }, Looper.myLooper()
        )
    }

    fun checkSelfPermission(): Boolean {
        var temp = false
        if ((ActivityCompat.checkSelfPermission(
                requireContext(), ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
                requireContext(), ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            temp = true
        }
        return temp
    }

    fun requestPermission() {

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            ), MY_LOCATION_PERMISSION
        )
    }

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    fun enableLocationServices() {
        val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getFreshLocation()
            }
        }
    }

}