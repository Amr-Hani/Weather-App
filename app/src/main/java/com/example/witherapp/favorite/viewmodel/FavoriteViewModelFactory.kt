package com.example.witherapp.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.witherapp.model.IRepo

class FavoriteViewModelFactory(private val repo: IRepo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            FavoriteViewModel(repo) as T
        } else {
            throw IllegalArgumentException("Not Found View Model Class")
        }
    }
}