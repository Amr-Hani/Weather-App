package com.example.witherapp.ui.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.witherapp.MyKey
import com.example.witherapp.Notification
import com.example.witherapp.R
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.model.Repo
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.ui.alarm.view.AlarmFragment
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModel
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {
    var temp: Int = 0
    var latitude: Double? = null
    var longitude: Double? = null

    lateinit var sharedPreferences: SharedPreferences

    lateinit var alarmViewModel: AlarmViewModel
    lateinit var alarmViewModelFactory: AlarmViewModelFactory


    companion object {
        const val CHANNEL_ID = "WeatherAlertChannel"
        const val NOTIFICATION_ID = 200
        const val ACTION_DISMISS = "com.example.weatherwise.DISMISS_ALERT"
        private var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "onReceive: ${AlarmFragment.primaryKey}")


        latitude = intent.extras?.getDouble("lat")
        longitude = intent.extras?.getDouble("long")
        Log.d("TAG", "onReceive: lat $latitude , lon $longitude")
        when (intent.action) {
            ACTION_DISMISS -> {
                dismissAlert(context)
            }

            else -> CoroutineScope(Dispatchers.IO).launch {
                val response =
                    Repo.getInstance(
                        RemoteDataSource.getInstance(
                            RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                        ),
                        LocalDataSource(
                            MyRoomDatabase.getInstance(context).getAllFavoritePlace()
                        )
                    ).getWitherOfTheDay(latitude!!, longitude!!, "en")
                response.catch {
                    Toast.makeText(
                        context.applicationContext,
                        "Failed to get temperature",
                        Toast.LENGTH_SHORT
                    ).show()
                }.collect {
                    withContext(Dispatchers.Main) {
                        temp = it.main.temp.toInt()
                        showNotification(context, latitude!!, longitude!!)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, latitude: Double, longitude: Double) {

        sharedPreferences = context.getSharedPreferences(
            MyKey.MY_SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val notification = sharedPreferences.getString(
            MyKey.NOTIFICATION_KEY, Notification.enabled.toString()
        )

        val channelId = CHANNEL_ID
        createNotificationChannel(context, channelId)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openFragmentIntent = Intent(context, AlarmFragment::class.java)
        val openFragmentPendingIntent = PendingIntent.getActivity(
            context, 0, openFragmentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val country = getLocationName(context, latitude, longitude)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.mist)
            .setContentTitle("Weather Alert")
            .setContentText("The temp is $temp is $country")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                dismissPendingIntent,
            )
            .setAutoCancel(true)
            .setSound(null)  // Disable notification sound
            .setContentIntent(openFragmentPendingIntent)

        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())

//        val notificationOrAlarm =
//            context.getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS, Context.MODE_PRIVATE)
//                .getString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "alarm")
//
//        // Play the sound manually
//        if (notificationOrAlarm == "alarm") {
//            playSound(context, soundUri)
//            }
//}
        if (notification == Notification.enabled.toString()) {
            playSound(context, soundUri)
        }
    }

    private fun playSound(context: Context, soundUri: android.net.Uri) {
        ringtone = RingtoneManager.getRingtone(context, soundUri)
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.let { audioManager ->
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone?.play()
            }
        }
    }

    private fun dismissAlert(context: Context) {
        // Stop the ringtone
        ringtone?.stop()

        // Cancel the notification
        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Channel for weather alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(null, null)  // Disable sound for the channel
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context)
        val readableLocation = geocoder.getFromLocation(latitude, longitude, 1)
        val address = readableLocation?.get(0)

        val country = address?.countryName ?: "Unknown Country"
        return country
    }
}

