package com.example.witherapp.favorite.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.witherapp.ApiState
import com.example.witherapp.database.LocalDataSource
import com.example.witherapp.database.MyRoomDatabase
import com.example.witherapp.databinding.FragmentFavoriteBinding
import com.example.witherapp.favorite.viewmodel.FavoriteViewModel
import com.example.witherapp.favorite.viewmodel.FavoriteViewModelFactory
import com.example.witherapp.model.FavoritePlace
import com.example.witherapp.model.Repo
import com.example.witherapp.network.ApiServices
import com.example.witherapp.network.RetrofitHelper
import com.example.witherapp.network.RemoteDataSource
import com.example.witherapp.ui.home.view.HomeFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment(),OnClickListner<FavoritePlace> {

    lateinit var binding: FragmentFavoriteBinding
    lateinit var favoriteViewModel: FavoriteViewModel
    lateinit var favoriteViewModelFactory: FavoriteViewModelFactory
    lateinit var favoritePlaceAdapter: FavoritePlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritePlaceAdapter = FavoritePlaceAdapter(this)
        favoriteViewModelFactory = FavoriteViewModelFactory(
            Repo.getInstance(
                RemoteDataSource.getInstance(
                    RetrofitHelper.retrofitInstance.create(ApiServices::class.java)
                ),
                LocalDataSource(
                    MyRoomDatabase.getInstance(requireContext()).getAllFavoritePlace()
                )
            )
        )
        favoriteViewModel =
            ViewModelProvider(this, favoriteViewModelFactory).get(FavoriteViewModel::class.java)
        binding.rvFavoritePlace.apply {
            adapter = favoritePlaceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        favoriteViewModel.getAllFavoriteProduct()
        lifecycleScope.launch {
        favoriteViewModel.favoritePlaceStateFlow.collectLatest {
            when(it){
                is ApiState.Failure -> Log.d("TAG", "onViewCreated: ApiState.Failure")
                is ApiState.Loading -> Log.d("TAG", "onViewCreated: ApiState.Loading")
                is ApiState.Success -> favoritePlaceAdapter.submitList(it.data)

            }}
        }
        if(HomeFragment.isConnected)
        {
            binding.FAB.setOnClickListener() {

                val action = FavoriteFragmentDirections.actionNavFavoriteToMapsFragment()

                Navigation.findNavController(view).navigate(action)
            }
        }
        else{
            Toast.makeText(requireContext(),"No Internet",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClicK(pojo: FavoritePlace) {
        favoriteViewModel.delete(pojo)
    }


}