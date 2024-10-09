package com.example.witherapp.ui.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.witherapp.ApiState
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.IRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlarmViewModel(val repo: IRepo) : ViewModel() {
    private val singleAlarmMutableStateFlow =      MutableStateFlow<ApiState<List<SingleAlarm>>>(
        ApiState.Loading())
    val alarmMangerStateFlow = singleAlarmMutableStateFlow.asStateFlow()

    suspend fun insertAlarmLocation(singleAlarm: SingleAlarm): Long {
        getAllAlarmLocation()
        return  repo.insertAlarmLocation(singleAlarm)
    }

    fun deleteAlarmLocation(singleAlarm:SingleAlarm):Int {
        var result = 0
        viewModelScope.launch(Dispatchers.IO) {
            result = repo.deleteAlarmLocation(singleAlarm)
            getAllAlarmLocation()
        }
        getAllAlarmLocation()
        return result
    }

    fun getAllAlarmLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllAlarmLocation().catch {
                singleAlarmMutableStateFlow.value = ApiState.Failure(it.toString())
            }.collect {
                singleAlarmMutableStateFlow.value = ApiState.Success(it)
            }
        }
    }


}