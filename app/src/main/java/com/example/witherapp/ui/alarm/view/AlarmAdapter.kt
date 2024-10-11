package com.example.witherapp.ui.alarm.view

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.witherapp.MyKey
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.ShowAlarmItemBinding
import com.example.witherapp.favorite.view.OnClickListner
import com.example.witherapp.model.Repo
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModel
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModelFactory

class AlarmAdapter(val onClickListner: OnClickListner<SingleAlarm>) : ListAdapter<SingleAlarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffUtil()) {
    lateinit var binding: ShowAlarmItemBinding
    lateinit var language: String
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ShowAlarmItemBinding.inflate(layoutInflater)
        sharedPreferences = parent.context.getSharedPreferences(
            MyKey.MY_SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )
        language = sharedPreferences.getString(MyKey.LANGUAGE_KEY, "en") ?: "en"
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currentAlarmManager = getItem(position)
        Log.d("TAG", "onBindViewHolder: ${currentAlarmManager.address} ")
        holder.binding.tvAlarm.text = currentAlarmManager.address
        holder.binding.btnAlarm.setOnClickListener{
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Deletion")
                .setMessage("Do You Want Remove This Place From Favorite")
                .setPositiveButton("Yes") { dialog, _ ->
                    onClickListner.onClicK(currentAlarmManager)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    class AlarmViewHolder(val binding: ShowAlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}