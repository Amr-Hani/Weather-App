package com.example.witherapp.ui.home.view

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.witherapp.databinding.WitherForcastFiveWeakBinding
import com.example.witherapp.model.WitherForecastItem
import com.example.witherapp.ui.home.DiffUtil
import com.example.witherapp.ui.home.view.DailyHourAdapter.DailyHourViewHolder
import java.text.SimpleDateFormat
import java.util.Locale

private const val IMG_URL = "https://openweathermap.org/img/wn/"
private const val END_POINT = ".png"

class WitherForCastAdapter :
    ListAdapter<WitherForecastItem, WitherForCastAdapter.WitherForCastViewHolder>(DiffUtil()) {
    lateinit var binding: WitherForcastFiveWeakBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var language: String
    lateinit var unit: String


    class WitherForCastViewHolder(val binding: WitherForcastFiveWeakBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WitherForCastViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = WitherForcastFiveWeakBinding.inflate(layoutInflater, parent, false)
        sharedPreferences =
            parent.context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        language = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unit = sharedPreferences.getString("UNIT", "celsius") ?: "celsius"

        return WitherForCastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WitherForCastViewHolder, position: Int) {
        val currentWeatherForCast = getItem(position)
        holder.binding.tvDay.text = convertToDays(currentWeatherForCast.dt_txt)
        holder.binding.tvDiscretion.text = currentWeatherForCast.weather.get(0).description

        showTemp(holder,currentWeatherForCast)

        val imgUrl = "$IMG_URL${currentWeatherForCast.weather[0].icon}$END_POINT"
        Glide.with(holder.itemView.context)
            .load(imgUrl)
            .into(holder.binding.ivWitherForCast)

    }

    fun convertToDays(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val date = inputFormat.parse(date)
        return outputFormat.format(date)
    }

    fun showTemp(holder: WitherForCastViewHolder,currentWeatherForCast:WitherForecastItem) {
        when (language) {
            "ar" ->
                when (unit) {
                    "celsius" -> {
                        holder.binding.tvTempWitherForCast.text =
                            "${currentWeatherForCast.main.temp_max}/${currentWeatherForCast.main.temp_min}س "
                      }
                    "kelvin" -> {
                        holder.binding.tvTempWitherForCast.text =
                            "${convertFromCelsiusToKelvin(currentWeatherForCast.main.temp_max)}/${convertFromCelsiusToKelvin(currentWeatherForCast.main.temp_min)}ك "
                      }

                    "fehrenheit" -> {
                        holder.binding.tvTempWitherForCast.text =
                            "${convertFromCelsiusToFahrenheit(currentWeatherForCast.main.temp_max)}/${convertFromCelsiusToFahrenheit(currentWeatherForCast.main.temp_min)}ف "
                    }
                }

            else -> {
                when (unit) {
                    "celsius" -> {
                        holder.binding.tvTempWitherForCast.text =
                            "${currentWeatherForCast.main.temp_max}/${currentWeatherForCast.main.temp_min}°C"
                      }

                    "kelvin" -> {

                        holder.binding.tvTempWitherForCast.text =
                            "${convertFromCelsiusToKelvin(currentWeatherForCast.main.temp_max)}/${convertFromCelsiusToKelvin(currentWeatherForCast.main.temp_min)} K"
                      }

                    "fehrenheit" -> {

                        holder.binding.tvTempWitherForCast.text =
                            "${convertFromCelsiusToFahrenheit(currentWeatherForCast.main.temp_max)}/${convertFromCelsiusToFahrenheit(currentWeatherForCast.main.temp_min)} F"
                        }
                }
            }
        }
    }
    fun convertFromCelsiusToFahrenheit(temp: Double): Int {
        return ((temp * 1.8) + 32).toInt()
    }

    fun convertFromCelsiusToKelvin(temp: Double): Int {
        return (temp + 273.15).toInt()
    }
}