package com.example.witherapp.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.witherapp.ApiState
import com.example.witherapp.model.CurrentWeatherResponse
import com.example.witherapp.model.Repo
import com.example.witherapp.model.WitherForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

class HomeViewModel(val repo: Repo) : ViewModel() {

    private val witherOfTheDayMutableStateFlow = MutableStateFlow<ApiState<CurrentWeatherResponse>>(ApiState.Loading())
    val witherOfTheDayStateFlow = witherOfTheDayMutableStateFlow.asStateFlow()

    fun getWitherOfTheDay(lat:Double,long:Double,language:String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getWitherOfTheDay(lat,long,language)
                .catch {
                    witherOfTheDayMutableStateFlow.value = ApiState.Failure(it.toString())
                }.collect{
                    witherOfTheDayMutableStateFlow.value = ApiState.Success(it)
                }
            Log.d("TAG", "onStart: viewModelScope FAVORITE")

        }
    }

    private val witherForCastMutableStateFlow = MutableStateFlow<ApiState<WitherForecastResponse>>(ApiState.Loading())
    val witherForCastStateFlow = witherForCastMutableStateFlow.asStateFlow()

    fun getWitherForCast(lat:Double,long:Double,language:String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getWitherForCast(lat,long,language)
                .catch {
                    witherForCastMutableStateFlow.value = ApiState.Failure(it.toString())
                }.collect{
                    witherForCastMutableStateFlow.value = ApiState.Success<WitherForecastResponse>(it)
                }
        }
    }

}