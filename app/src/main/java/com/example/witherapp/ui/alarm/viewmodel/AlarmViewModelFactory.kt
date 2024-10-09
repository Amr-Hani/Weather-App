package com.example.witherapp.ui.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.witherapp.model.IRepo
import com.example.witherapp.ui.home.viewmodel.HomeViewModel

class AlarmViewModelFactory(private val repo: IRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            AlarmViewModel(repo) as T
        } else {
            throw IllegalArgumentException(" class view  model not fount")
        }
    }
}