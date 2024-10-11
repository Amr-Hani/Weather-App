package com.example.witherapp.map

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.witherapp.R
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentMapsBinding
import com.example.witherapp.favorite.viewmodel.FavoriteViewModel
import com.example.witherapp.favorite.viewmodel.FavoriteViewModelFactory
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.FavoritePlace
import com.example.witherapp.model.Repo
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.ui.alarm.AlarmReceiver
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModel
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModelFactory

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class MapsFragment : Fragment(), OnMapReadyCallback {
    lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var locationAddress: String? = null
    private var currentMarker: Marker? = null
    lateinit var binding: FragmentMapsBinding
    lateinit var favoriteViewModel: FavoriteViewModel
    lateinit var favoriteViewModelFactory: FavoriteViewModelFactory
    lateinit var navigate: String

    lateinit var alarmViewModel: AlarmViewModel
    lateinit var alarmViewModelFactory: AlarmViewModelFactory


    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var pendingIntent: PendingIntent

    private lateinit var alarmManager: AlarmManager

    lateinit var singleAlarm: SingleAlarm


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        navigate = MapsFragmentArgs.fromBundle(requireArguments()).map

        favoriteViewModelFactory = FavoriteViewModelFactory(
            Repo.getInstance(
                RemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                LocalDataSource(
                    MyRoomDatabase.getInstance(requireContext()).getAllFavoritePlace()
                )
            )
        )
        favoriteViewModel =
            ViewModelProvider(this, favoriteViewModelFactory).get(FavoriteViewModel::class.java)


        alarmViewModelFactory = AlarmViewModelFactory(
            Repo.getInstance(
                RemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                LocalDataSource(
                    MyRoomDatabase.getInstance(requireContext()).getAllFavoritePlace()
                )
            )
        )

        alarmViewModel =
            ViewModelProvider(this, alarmViewModelFactory).get(AlarmViewModel::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val myHome = LatLng(30.651783424151727, 31.6327091306448)
        currentMarker =
            map.addMarker(MarkerOptions().position(myHome).title("Hod Negeih, Hihya, Al-Sharqia"))
        map.moveCamera(CameraUpdateFactory.newLatLng(myHome))

        map.setOnMapClickListener { latLng ->
            currentMarker?.remove()
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))

            Log.d("TAG", "onMapReady: ${latLng.latitude}        ${latLng.longitude}")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            latitude = latLng.latitude
            longitude = latLng.longitude
            Log.d("TAG", "onMapReady:  الونج والات بيتبعت ولا لا  $latitude   >> $longitude")
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                singleAlarm = SingleAlarm(address = address.getAddressLine(0).toString(), date = "")

                locationAddress = address.getAddressLine(0)
                currentMarker =
                    map.addMarker(MarkerOptions().position(latLng).title(locationAddress))
                binding.addFavorite.visibility = View.VISIBLE
                binding.addFavorite.setOnClickListener {
                    val favoritePlace = FavoritePlace(
                        address = addresses.toString(),
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        when (navigate) {
                            "MAP" -> {
                                withContext(Dispatchers.Main)
                                {
                                    val action =
                                        MapsFragmentDirections.actionMapsFragmentToNavHome()
                                            .apply {
                                                latLong =
                                                    "MAP,${favoritePlace.latitude},${favoritePlace.longitude}"
                                            }
                                    Navigation.findNavController(binding.root)
                                        .navigate(action)
                                }
                            }

                            "Alarm" -> {
                                withContext(Dispatchers.Main)
                                {
                                    showAndSitTime()
                                }
                            }

                            else -> {
                                val result = favoriteViewModel.insert(favoritePlace)
                                withContext(Dispatchers.Main)
                                {
                                    if (result > 0) {
                                        Toast.makeText(
                                            requireContext(),
                                            "كدا انت حبيبي",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val action =
                                            MapsFragmentDirections.actionMapsFragmentToNavFavorite()
                                        Navigation.findNavController(binding.root)
                                            .navigate(action)
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "كدا انا زعلت",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }
                        }

                    }

                }
            } else {
                Log.d("TAG", "onMapReady: Address not found!")
            }
        }
    }

    private fun showAndSitTime() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }
                showAndSitDate(selectedDate)
            },
            year, month, day
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun showAndSitDate(selectedDate: Calendar) {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedDate.apply {

                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                }
                if (selectedDate.timeInMillis <= System.currentTimeMillis()) {
                    Toast.makeText(requireContext(), "Select vaield Time", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    createdAlarm(selectedDate)
                    singleAlarm.date = selectedDate.timeInMillis.toString()
                }
            },
            hour, minute, false
        ).show()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun createdAlarm(selectedDateTime: Calendar) {

        val alarmId = System.currentTimeMillis().toInt()
        singleAlarm.primaryKey = alarmId

        Log.d("TAG", "scheduleAlarm: الونج والات بيتبعت ولا لا  $latitude   >> $longitude")
        val intent = Intent(requireContext(), AlarmReceiver::class.java)

        intent.putExtra("alarmId", alarmId)
        intent.putExtra("lat", latitude)
        intent.putExtra("long", longitude)

        pendingIntent = PendingIntent.getBroadcast(
            requireContext(), alarmId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager
                .RTC_WAKEUP,
            selectedDateTime.timeInMillis,
            pendingIntent
        )
        lifecycleScope.launch(Dispatchers.IO) {
            val result = alarmViewModel.insertAlarmLocation(singleAlarm)
            withContext(Dispatchers.Main)
            {
                if (result > 0) {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    val action = MapsFragmentDirections.actionMapsFragmentToNavAlarm()
                    Navigation.findNavController(binding.root).navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Field", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}