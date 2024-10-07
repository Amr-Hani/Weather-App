package com.example.witherapp.favorite.view

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.witherapp.databinding.FavoritePlaceItemBinding
import com.example.witherapp.model.FavoritePlace

class FavoritePlaceAdapter(val onClickListner: OnClickListner<FavoritePlace>) :
    ListAdapter<FavoritePlace, FavoritePlaceAdapter.FavoritePlaceViewHolder>(MyFavoriteDiffUtil()) {
    lateinit var binding: FavoritePlaceItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlaceViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FavoritePlaceItemBinding.inflate(layoutInflater, parent, false)
        return FavoritePlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritePlaceViewHolder, position: Int) {
        val currentFavoritePlace = getItem(position)
        holder.binding.tvFavoriteNamePlace.text = getLocationName(
            currentFavoritePlace.latitude,
            currentFavoritePlace.longitude,
            holder.itemView.context
        )
        holder.binding.btnDeletePlace.setOnClickListener() {
            onClickListner.onClicK(currentFavoritePlace)
        }
        holder.binding.cvFavoritePlace.setOnClickListener() {
            val action =
                FavoriteFragmentDirections.actionNavFavoriteToNavHome().apply {
                    latLong ="FAVORITE,${currentFavoritePlace.latitude},${currentFavoritePlace.longitude}"
                }
            Navigation.findNavController(binding.root).navigate(action)
        }
    }

    class FavoritePlaceViewHolder(val binding: FavoritePlaceItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun getLocationName(latitude: Double, longitude: Double, c: Context): String {
        val geocoder = Geocoder(c)
        val readableLocation = geocoder.getFromLocation(latitude, longitude, 1)
        val address = readableLocation?.get(0)

        val city = address?.subLocality ?: address?.locality ?: "Unknown City"
        val state = address?.adminArea ?: "Unknown State"
        val country = address?.countryName ?: "Unknown Country"

        return "$city, $state ,$country"
    }

}