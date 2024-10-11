package com.example.witherapp.ui.alarm.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.example.witherapp.ApiState
import com.example.witherapp.MyKey
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentGalleryBinding
import com.example.witherapp.favorite.view.OnClickListner
import com.example.witherapp.model.Repo
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.ui.alarm.AlarmReceiver
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModel
import com.example.witherapp.ui.alarm.viewmodel.AlarmViewModelFactory
import com.example.witherapp.ui.home.view.HomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlarmFragment : Fragment(), OnClickListner<SingleAlarm> {

    lateinit var binding: FragmentGalleryBinding
    lateinit var alarmViewModel: AlarmViewModel
    lateinit var alarmViewModelFactory: AlarmViewModelFactory
    lateinit var alarmAdapter: AlarmAdapter

    companion object {
        var primaryKey = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmAdapter = AlarmAdapter(this)
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

        binding.recyclerView.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        alarmViewModel.getAllAlarmLocation()
        lifecycleScope.launch {

            alarmViewModel.alarmMangerStateFlow.collectLatest {

                when (it) {
                    is ApiState.Failure -> {
                        Log.d("TAG", "onViewCreated: failure ")
                    }
                    is ApiState.Loading -> {
                        Log.d("TAG", "onViewCreated: loading ")
                    }
                    is ApiState.Success -> {
                        it.data.forEach{
                            if(it.date.toLong() <= System.currentTimeMillis())
                            {
                                alarmViewModel.deleteAlarmLocation(it)
                            }
                        }
                        alarmAdapter.submitList(it.data)
                     }
                }
            }
        }

        val alarm = AlarmFragmentArgs.fromBundle(requireArguments()).alarm
        Log.d("TAG", "onViewCreated: $alarm")
        if (HomeFragment.isConnected) {
            binding.floatingActionButton.setOnClickListener {
                val action = AlarmFragmentDirections.actionNavAlarmToMapsFragment()
                    .apply {
                        map = "Alarm"
                    }
                Navigation.findNavController(binding.root).navigate(action)
            }
        } else {
            Toast.makeText(requireContext(), "No Internet", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClicK(pojo: SingleAlarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmId", pojo.primaryKey)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pojo.primaryKey,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        alarmViewModel.deleteAlarmLocation(pojo)
    }
}
