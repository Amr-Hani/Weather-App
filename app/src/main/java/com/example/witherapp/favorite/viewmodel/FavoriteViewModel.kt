package com.example.witherapp.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.witherapp.ApiState
import com.example.witherapp.model.FavoritePlace
import com.example.witherapp.model.IRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repo: IRepo) : ViewModel() {
    private var mutableFavoritePlaceStateFlow =
        MutableStateFlow<ApiState<List<FavoritePlace>>>(ApiState.Loading())
    val favoritePlaceStateFlow = mutableFavoritePlaceStateFlow.asStateFlow()


    suspend fun insert(favoritePlace: FavoritePlace): Long {
        getAllFavoriteProduct()
        return  repo.insertFavoritePlace(favoritePlace)
    }

    fun delete(favoritePlace: FavoritePlace):Int {
        var result = 0
        viewModelScope.launch(Dispatchers.IO) {
            result = repo.deleteFavoritePlace(favoritePlace)
            getAllFavoriteProduct()
        }
        getAllFavoriteProduct()
        return result
    }

    fun getAllFavoriteProduct() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllFavoritePlace().catch {
                mutableFavoritePlaceStateFlow.value = ApiState.Failure(it.toString())
            }.collect {
                mutableFavoritePlaceStateFlow.value = ApiState.Success(it)
            }
        }
    }

}