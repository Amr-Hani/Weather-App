package com.example.witherapp.ui.setting

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
import com.example.witherapp.Language
import com.example.witherapp.Location
import com.example.witherapp.MyKey
import com.example.witherapp.Notification
import com.example.witherapp.Units
import com.example.witherapp.Wind
import com.example.witherapp.databinding.FragmentSlideshowBinding

class SettingFragment : Fragment() {
    lateinit var binding: FragmentSlideshowBinding
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(MyKey.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLanguage()
        getUnit()
        getLocation()
        getWindSpeed()
        getNotification()
    }


    fun getLanguage() {
        val language = sharedPreferences.getString(MyKey.LANGUAGE_KEY, Language.ar.toString())
        when (language) {
            "ar" -> binding.rbArabic.isChecked = true
            "en" -> binding.rbEnglish.isChecked = true
        }
        binding.rgLanguage.setOnCheckedChangeListener() { groub, checkedId ->
            if (binding.rbArabic == binding.root.findViewById(checkedId)) {
                Toast.makeText(requireContext(), "arabic", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString(MyKey.LANGUAGE_KEY, Language.ar.toString()).apply()
                Thread.sleep(500)
                changeLanguage("ar")
                //restartApp()
            } else {
                Toast.makeText(requireContext(), "english", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString(MyKey.LANGUAGE_KEY,Language.en.toString()).apply()
                Thread.sleep(500)
                changeLanguage("en")
                //restartApp()
            }
        }
    }

    fun getUnit() {
        val unit = sharedPreferences.getString(MyKey.UNIT_KEY, Units.celsius.toString())
        when (unit) {
            "celsius" -> binding.rbCelsius.isChecked = true
            "kelvin" -> binding.rbKelvin.isChecked = true
            "fehrenheit" -> binding.rbFehrenheit.isChecked = true
        }
        binding.rgTemperature.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbKelvin == binding.root.findViewById(checkedId)) {
                sharedPreferences.edit().putString(MyKey.UNIT_KEY, Units.kelvin.toString()).apply()
                Toast.makeText(requireContext(), Units.kelvin.toString(), Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                //restartApp()
            } else if (binding.rbFehrenheit == binding.root.findViewById(checkedId)) {
                sharedPreferences.edit().putString(MyKey.UNIT_KEY, Units.fehrenheit.toString()).apply()
                Toast.makeText(requireContext(), Units.fehrenheit.toString(), Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                //restartApp()
            } else {
                sharedPreferences.edit().putString(MyKey.UNIT_KEY, Units.celsius.toString()).apply()
                Toast.makeText(requireContext(), Units.celsius.toString(), Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                //restartApp()
            }

        }
    }

    fun getLocation() {
//        val location = sharedPreferences.getString(MyKey.LOCATION_KEY, Location.gps.toString())
//        when (location) {
//            Location.map.toString() -> binding.rbMap.isChecked = true
//            Location.gps.toString() -> binding.rbGps.isChecked = true
//        }
        binding.rgLocation.setOnCheckedChangeListener { group, checkedId ->
            if (binding.rbGps == binding.root.findViewById(checkedId)) {
                //sharedPreferences.edit().putString(MyKey.LOCATION_KEY, Location.gps.toString()).apply()
                //Thread.sleep(500)
                val action = SettingFragmentDirections.actionNavSettingToNavHome()
                Navigation.findNavController(binding.root).navigate(action)
            } else if (binding.rbMap == binding.root.findViewById(checkedId)){
                //sharedPreferences.edit().putString(MyKey.LOCATION_KEY, Location.map.toString()).apply()
                //Thread.sleep(500)
                val action = SettingFragmentDirections.actionNavSettingToMapsFragment().apply {
                    map = "MAP"
                }
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }

    fun getWindSpeed()
    {
        val wind = sharedPreferences.getString(MyKey.WIND_KEY, Wind.meter.toString())
        when (wind) {
            Wind.meter.toString() -> binding.rbMeter.isChecked = true
            Wind.mile.toString() -> binding.rbMile.isChecked = true
        }
        binding.rgWindSpeed.setOnCheckedChangeListener { group, checkedId ->
            if ( binding.rbMile == binding.root.findViewById(checkedId))
            {
                sharedPreferences.edit().putString(MyKey.WIND_KEY,Wind.mile.toString()).apply()
                Thread.sleep(500)
//                restartApp()
            }else
            {
                sharedPreferences.edit().putString(MyKey.WIND_KEY,Wind.meter.toString()).apply()
                Thread.sleep(500)
//                restartApp()
            }
        }
    }

 fun getNotification()
    {
        val notification = sharedPreferences.getString(MyKey.NOTIFICATION_KEY, Notification.enabled.toString())
        when (notification) {
            Notification.enabled.toString() -> binding.rbEnabled.isChecked = true
            Notification.disabled.toString() -> binding.rbDisabled.isChecked = true
        }
        binding.rgNotification.setOnCheckedChangeListener { group, checkedId ->
            if ( binding.rbDisabled == binding.root.findViewById(checkedId))
            {
                sharedPreferences.edit().putString(MyKey.NOTIFICATION_KEY,Notification.disabled.toString()).apply()
                Thread.sleep(500)
                //restartApp()
            }else
            {
                sharedPreferences.edit().putString(MyKey.NOTIFICATION_KEY,Notification.enabled.toString()).apply()
                Thread.sleep(500)
                //restartApp()
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

    fun reUpDateChangedApp(fragment: Fragment) {
        val fragmentManager = fragment.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        this.activity?.recreate()
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commit()
    }



}