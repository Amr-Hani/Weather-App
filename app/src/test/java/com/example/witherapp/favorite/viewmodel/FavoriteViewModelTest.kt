package com.example.witherapp.favorite.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.witherapp.FakeRepo
import com.example.witherapp.ui.home.viewmodel.HomeViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteViewModelTest {
    lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        homeViewModel = HomeViewModel(FakeRepo())
    }

    @Test
    fun getCurrentWeather_Succes() = runBlocking {

        // When
        val result = homeViewModel.getWitherOfTheDay(165310.0 , 41630.0 , "en")

        // Then
        assertThat(result, not(nullValue()))
    }

    @Test
    fun getFiveDayesForecast_Succes() = runBlocking {

        val result = homeViewModel.getWitherForCast(0.0 , 0.0 , "en")

        assertThat(result, not(nullValue()))

    }

}