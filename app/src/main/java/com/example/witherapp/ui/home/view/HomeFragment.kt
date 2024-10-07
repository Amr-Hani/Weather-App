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
import com.example.witherapp.database.FavoritePlaceLocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentHomeBinding
import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.Repo
import com.example.witherapp.model.WitherForecastItem
import com.example.witherapp.model.WitherForecastResponse
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.network.WitherOfTheDayRemoteDataSource
import com.example.witherapp.ui.home.viewmodel.HomeViewModel
import com.example.witherapp.ui.home.viewmodel.HomeViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

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
    lateinit var wind:String

    var max = Double.MIN_VALUE
    var min = Double.MAX_VALUE
    override fun onStart() {
        super.onStart()

        sharedPreferences =
            requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        language = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unit = sharedPreferences.getString("UNIT", "celsius") ?: "celsius"
        wind = sharedPreferences.getString("WIND", "meter")?:"meter"
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


        when (message) {
            "FAVORITE" -> {
                getFavoriteLocation(latitude.toDouble(), longitude.toDouble())
                Log.d("TAG", "onStart: message FAVORITE")
            }
            "MAP"->{
                getFavoriteLocation(latitude.toDouble(),longitude.toDouble())
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
                WitherOfTheDayRemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                FavoritePlaceLocalDataSource(
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

        currentWitherOfTheDayLifeCycle()
        forCastWitherOfTheWeekLifeCycle()
    }

    fun currentWitherOfTheDayLifeCycle() {
        lifecycleScope.launch {
            homeViewModel.witherOfTheDayStateFlow.collectLatest {
                when (it) {
                    is ApiState.Loading -> {
                        Log.d("TAG", "onCreateView: Loading witherOfTheDay")
                    }

                    is ApiState.Success -> {
                        currentWitherOfTheDaySuccess(it.data, language)
                    }

                    else -> {
                        Toast.makeText(requireContext(), "ابو تقل دم امك", Toast.LENGTH_LONG).show()
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
                        binding.tvTempreture.text = "${convertFromCelsiusToKelvin(currentDay.main.temp)} ك "
                        binding.tvFeelsLike.text = "${convertFromCelsiusToKelvin(currentDay.main.feels_like)} ك "
                    }
                    "fehrenheit" -> {
                        binding.tvTempreture.text = "${convertFromCelsiusToFahrenheit(currentDay.main.temp)}ف "
                        binding.tvFeelsLike.text = "${convertFromCelsiusToFahrenheit(currentDay.main.feels_like)}ف "
                    }

                }
                when(wind)
                {
                    "mile"->{
                        binding.tvWindValue.text = "${convertMeterPerSecToMilePerHour(currentDay.wind.speed)}ميل/س "
                    }
                    else->{binding.tvWindValue.text = "${currentDay.wind.speed}م/ث "}
                }
                binding.tvDescription.text = currentDay.weather.get(0).description
                Log.d("TAG", "onCreateView: Success witherOfTheDay")
                binding.tvPressureValue.text = "${currentDay.main.pressure}ض.ج "
                binding.tvHumidityValue.text = "${currentDay.main.humidity} % "

                binding.tvCloudValue.text = "${currentDay.clouds.all} %"
                binding.tvVisibilityValue.text = "${currentDay.visibility} م "
                binding.tvLocation.text =
                    getReadableLocation(currentDay.coord.lat, currentDay.coord.lon)
                Log.d(
                    "TAG",
                    "onCreateView: getReadableLocationArabic(currentDay.coord.lat, currentDay.coord.lon) ${
                        getReadableLocation(
                            currentDay.coord.lat,
                            currentDay.coord.lon
                        )
                    }"
                )
            }

            "en" -> {
                when (unit) {
                    "celsius" -> {
                        binding.tvTempreture.text = "${currentDay.main.temp} °C"
                        binding.tvFeelsLike.text = "${currentDay.main.feels_like} °C"
                    }
                    "kelvin" -> {
                        binding.tvTempreture.text = "${convertFromCelsiusToKelvin(currentDay.main.temp)} K"
                        binding.tvFeelsLike.text = "${convertFromCelsiusToKelvin(currentDay.main.feels_like)} K"
                        binding.tvLocation.text =
                            getReadableLocation(currentDay.coord.lat, currentDay.coord.lon)
                    }
                    "fehrenheit" -> {
                        binding.tvTempreture.text = "${convertFromCelsiusToFahrenheit(currentDay.main.temp)} F"
                        binding.tvFeelsLike.text = "${convertFromCelsiusToFahrenheit(currentDay.main.feels_like)} F"
                    }

                }
                when(wind)
                {
                    "mile"->{
                        binding.tvWindValue.text = "${convertMeterPerSecToMilePerHour(currentDay.wind.speed)} Mile/Hr"
                    }
                    else->{binding.tvWindValue.text = "${currentDay.wind.speed} M/Sec"}
                }
                binding.tvDescription.text = currentDay.weather.get(0).description
                Log.d("TAG", "onCreateView: Success witherOfTheDay")
                binding.tvPressureValue.text = "${currentDay.main.pressure} hpa"
                binding.tvHumidityValue.text = "${currentDay.main.humidity} %"
                binding.tvCloudValue.text = "${currentDay.clouds.all} %"
                Log.d("TAG", "onCreateView: Success witherOfTheDay6154d64cd64c6d4s")
                binding.tvLocation.text =
                    getReadableLocation(currentDay.coord.lat, currentDay.coord.lon)
                binding.tvVisibilityValue.text = "${currentDay.visibility} m"
            }
        }

    }

    fun forCastWitherOfTheWeekLifeCycle() {
        lifecycleScope.launch {
            homeViewModel.witherForCastStateFlow.collect {
                when (it) {
                    is ApiState.Failure -> Log.d("TAG", "onCreateView: Fail witherForCast")
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
        Log.d("TAG", "onCreateView: haaaaaaaaaaaaa ${currentDay.get(0)}")
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
            Log.d("TAG", "onCreateView: nooooooooo ${wither.dt_txt.split(" ").get(0)}")
            if (wither.dt_txt.startsWith(currentDay.get(0)) || wither.dt_txt.startsWith(
                    tomorrowsDate
                )
            ) {
                mutwitherForCatMutableList.add(wither)
            }
        }
        dailyHourAdapter.submitList(mutwitherForCatMutableList)
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

    }

    fun getFavoriteLocation(latitude: Double, longtude: Double) {
        homeViewModel.getWitherOfTheDay(latitude, longtude, language)
        homeViewModel.getWitherForCast(latitude, longtude, language)
    }

    @SuppressLint("MissingPermission")
    fun getFreshLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(LocationRequest.Builder(20000).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build(), object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                Log.d("TAG", "onLocationResult: 3ezzat ${p0.locations.toString()}")

                val longitude = p0.locations.lastOrNull()?.longitude
                val latitude = p0.locations.lastOrNull()?.latitude

                if (latitude != null && longitude != null) {
                    homeViewModel.getWitherOfTheDay(latitude, longitude, language)
                    homeViewModel.getWitherForCast(latitude, longitude, language)
                }

                val currentLocation = p0.locations.lastOrNull()?.latitude
                Log.d("TAG", "onLocationResult: 3m ay haga$currentLocation")
                Log.d("TAG", "onLocationResult: ${p0.lastLocation?.longitude.toString()}")
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

    fun getReadableLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext())
        val readableLocation = geocoder.getFromLocation(latitude, longitude, 1)
        val address = readableLocation?.get(0)

        val city = address?.subLocality ?: address?.locality ?: "Unknown City"
        val state = address?.adminArea ?: "Unknown State"
        val country = address?.countryName ?: "Unknown Country"

        return "$city, $state ,$country"
    }

    fun convertFromCelsiusToFahrenheit(temp: Double): Int {
        return ((temp * 1.8) + 32).toInt()
    }

    fun convertFromCelsiusToKelvin(temp: Double): Int {
        return (temp + 273.15).toInt()
    }
    fun convertMeterPerSecToMilePerHour (m : Double ):Int
    {
        return (m*2.23694).toInt()
        }

}