package com.example.witherapp.ui.slideshow

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.witherapp.databinding.FragmentSlideshowBinding

class SettingFragment : Fragment() {
    lateinit var binding: FragmentSlideshowBinding
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLanguage()
        getUnit()
        getLocation()
        getWindSpeed()
    }


    fun getLanguage() {
        val language = sharedPreferences.getString("LANGUAGE", "en")
        when (language) {
            "ar" -> binding.rbArabic.isChecked = true
            "en" -> binding.rbEnglish.isChecked = true
        }
        binding.rgLanguage.setOnCheckedChangeListener() { groub, checkedId ->
            if (binding.rbArabic == binding.root.findViewById(checkedId)) {
                Toast.makeText(requireContext(), "arabic", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString("LANGUAGE", "ar").apply()
                Thread.sleep(500)
                changeLanguage("ar")
                restartApp()
            } else {
                Toast.makeText(requireContext(), "english", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString("LANGUAGE", "en").apply()
                Thread.sleep(500)
                changeLanguage("en")
                restartApp()
            }
        }
    }

    fun getUnit() {
        val unit = sharedPreferences.getString("UNIT", "celsius")
        when (unit) {
            "celsius" -> binding.rbCelsius.isChecked = true
            "kelvin" -> binding.rbKelvin.isChecked = true
            "fehrenheit" -> binding.rbFehrenheit.isChecked = true
        }
        binding.rgTemperature.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbKelvin == binding.root.findViewById(checkedId)) {
                sharedPreferences.edit().putString("UNIT", "kelvin").apply()
                Toast.makeText(requireContext(), "kelvin", Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                restartApp()
            } else if (binding.rbFehrenheit == binding.root.findViewById(checkedId)) {
                sharedPreferences.edit().putString("UNIT", "fehrenheit").apply()
                Toast.makeText(requireContext(), "fehrenheit", Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                restartApp()
            } else {
                sharedPreferences.edit().putString("UNIT", "celsius").apply()
                Toast.makeText(requireContext(), "celsius", Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                restartApp()
            }

        }
    }

    fun getLocation() {
        val location = sharedPreferences.getString("LOCATION", "gps")
        when (location) {
            "map" -> binding.rbMap.isChecked = true
            "gps" -> binding.rbGps.isChecked = true
        }
        binding.rgLocation.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbGps == binding.root.findViewById(checkedId)) {
                sharedPreferences.edit().putString("LOCATION", "gps").apply()
                Thread.sleep(500)
                val action = SettingFragmentDirections.actionNavSettingToNavHome()
                Navigation.findNavController(binding.root).navigate(action)
            } else {
                sharedPreferences.edit().putString("LOCATION", "map").apply()
                Thread.sleep(500)
                val action = SettingFragmentDirections.actionNavSettingToMapsFragment().apply {
                    map = "MAP"
                }
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }

    fun getWindSpeed()
    {
        val wind = sharedPreferences.getString("WIND", "meter")
        when (wind) {
            "meter" -> binding.rbMeter.isChecked = true
            "mile" -> binding.rbMile.isChecked = true
        }
        binding.rgWindSpeed.setOnCheckedChangeListener { group, checkedId ->
            if ( binding.rbMile == binding.root.findViewById(checkedId))
            {
                sharedPreferences.edit().putString("WIND","mile").apply()
                Thread.sleep(500)
                restartApp()
            }else
            {
                sharedPreferences.edit().putString("WIND","meter").apply()
                Thread.sleep(500)
                restartApp()
            }
        }
    }

 fun getNotification()
    {
        val notification = sharedPreferences.getString("NOTIFICATION", "enabled")
        when (notification) {
            "enabled" -> binding.rbEnglish.isChecked = true
            "mile" -> binding.rbDisabled.isChecked = true
        }
        binding.rgWindSpeed.setOnCheckedChangeListener { group, checkedId ->
            if ( binding.rbDisabled == binding.root.findViewById(checkedId))
            {
                sharedPreferences.edit().putString("NOTIFICATION","disabled").apply()
                Thread.sleep(500)
                restartApp()
            }else
            {
                sharedPreferences.edit().putString("NOTIFICATION","enabled").apply()
                Thread.sleep(500)
                restartApp()
            }
        }
    }


    private fun restartApp() {
        activity?.let {
            val intent = it.intent
            it.finish()
            startActivity(intent)
           }
       }

    private fun changeLanguage(language: String) {
        val languageCode = when (language) {
            "ar" -> "arabic"
            "en" -> "english"
            else -> "english"
        }
        sharedPreferences.edit().putString("Language", languageCode).apply()
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun reUpDateCangedApp(fragment: Fragment) {
        val fragmentManager = fragment.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        this.activity?.recreate()
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commit()
    }



}