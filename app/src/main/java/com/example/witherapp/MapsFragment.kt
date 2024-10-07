package com.example.witherapp

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
import com.example.witherapp.database.FavoritePlaceLocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentMapsBinding
import com.example.witherapp.favorite.viewmodel.FavoriteViewModel
import com.example.witherapp.favorite.viewmodel.FavoriteViewModelFactory
import com.example.witherapp.model.FavoritePlace
import com.example.witherapp.model.Repo
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.network.WitherOfTheDayRemoteDataSource

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
import java.util.Locale

class MapsFragment : Fragment(), OnMapReadyCallback {
    lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var locationAddress: String? = null
    private var currentMarker: Marker? = null
    lateinit var binding: FragmentMapsBinding
    lateinit var favoriteViewModel: FavoriteViewModel
    lateinit var favoriteViewModelFactory: FavoriteViewModelFactory
    lateinit var navigate:String

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
                WitherOfTheDayRemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                FavoritePlaceLocalDataSource(
                    MyRoomDatabase.getInstance(requireContext()).getAllFavoritePlace()
                )
            )
        )
        favoriteViewModel =
            ViewModelProvider(this, favoriteViewModelFactory).get(FavoriteViewModel::class.java)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // تحريك الكاميرا إلى نقطة معينة
        val myHome = LatLng(30.651783424151727, 31.6327091306448)
        currentMarker =
            map.addMarker(MarkerOptions().position(myHome).title("Hod Negeih, Hihya, Al-Sharqia"))
        map.moveCamera(CameraUpdateFactory.newLatLng(myHome))

        // التعامل مع الضغط على الخريطة لإضافة Marker جديد
        map.setOnMapClickListener { latLng ->
            currentMarker?.remove()
            // إضافة Marker في المكان الذي تم الضغط عليه

            // تحريك الكاميرا إلى الموقع الجديد
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))

            Log.d("TAG", "onMapReady: ${latLng.latitude}        ${latLng.longitude}")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                locationAddress = address.getAddressLine(0) // تخزين العنوان في المتغير
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
                        when(navigate)
                        {
                            "MAP"->{
                                withContext(Dispatchers.Main)
                                {
                                val action = MapsFragmentDirections.actionMapsFragmentToNavHome().apply { latLong = "MAP,${favoritePlace.latitude},${favoritePlace.longitude}" }
                                Navigation.findNavController(binding.root).navigate(action)
                            }}
                            else->{
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
                                        Navigation.findNavController(binding.root).navigate(action)
                                    } else {
                                        Toast.makeText(requireContext(), "كدا انا زعلت", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        }

                    }

                }
                // عرض العنوان (يمكن استبداله بحفظه أو استخدامه)
                println("Address: $locationAddress")
            } else {
                println("Address not found!")
            }
        }
    }
}