package com.example.witherapp.ui.home.view

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.witherapp.databinding.DailyHourBinding
import com.example.witherapp.model.WitherForecastItem
import com.example.witherapp.ui.home.DiffUtil
import java.text.SimpleDateFormat
import java.util.Locale

private const val IMG_URL = "https://openweathermap.org/img/wn/"
private const val END_POINT = ".png"

class DailyHourAdapter :
    ListAdapter<WitherForecastItem, DailyHourAdapter.DailyHourViewHolder>(DiffUtil()) {
    lateinit var binding: DailyHourBinding
    lateinit var sharedPreferences: SharedPreferences

    lateinit var language: String
    lateinit var unit: String


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyHourViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DailyHourBinding.inflate(layoutInflater, parent, false)
        sharedPreferences =
            parent.context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        language = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unit = sharedPreferences.getString("UNIT", "celsius") ?: "celsius"

        return DailyHourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyHourViewHolder, position: Int) {
        val currentWitherForecastItem = getItem(position)

        holder.binding.tvHour.text = convertTo12HourTime(currentWitherForecastItem.dt_txt)
        holder.binding.tvTemp.text = currentWitherForecastItem.main.temp.toString()
        showTemp(holder,currentWitherForecastItem)
        val imgUrl = "$IMG_URL${currentWitherForecastItem.weather[0].icon}$END_POINT"
        Glide.with(holder.itemView.context)
            .load(imgUrl)
            .into(holder.binding.ivDailyHour)

    }


    class DailyHourViewHolder(val binding: DailyHourBinding) : RecyclerView.ViewHolder(binding.root)

    fun convertTo12HourTime(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh a", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        return outputFormat.format(date!!)
    }


    fun showTemp(holder: DailyHourViewHolder,currentWitherForecastItem:WitherForecastItem) {
        when (language) {
            "ar" ->
                when (unit) {
                    "celsius" -> {
                        holder.binding.tvTemp.text = "${currentWitherForecastItem.main.temp}س "
                    }
                    "kelvin" -> {
                        holder.binding.tvTemp.text = "${convertFromCelsiusToKelvin(currentWitherForecastItem.main.temp)}ك "
                    }

                    "fehrenheit" -> {
                        holder.binding.tvTemp.text = "${convertFromCelsiusToFahrenheit(currentWitherForecastItem.main.temp)}ف "
                    }
                }

            else -> {
                when (unit) {
                    "celsius" -> {
                        holder.binding.tvTemp.text = "${currentWitherForecastItem.main.temp} °C"
                    }

                    "kelvin" -> {
                        holder.binding.tvTemp.text = "${currentWitherForecastItem.main.temp} K"
                    }

                    "fehrenheit" -> {
                        holder.binding.tvTemp.text = "${convertFromCelsiusToFahrenheit(currentWitherForecastItem.main.temp)} F"
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