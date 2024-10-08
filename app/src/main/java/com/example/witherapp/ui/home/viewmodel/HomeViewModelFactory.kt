package com.example.witherapp.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.witherapp.model.Repo

class HomeViewModelFactory(private val repo: Repo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java))
        {
            HomeViewModel(repo) as T
        }
        else
        {
            throw IllegalArgumentException(" class view  model not fount")
            }
    }
}