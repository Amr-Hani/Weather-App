package com.example.witherapp.favorite.view

import androidx.recyclerview.widget.DiffUtil
import com.example.witherapp.model.FavoritePlace


class MyFavoriteDiffUtil : DiffUtil.ItemCallback<FavoritePlace>() {
    override fun areItemsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace): Boolean {
        return oldItem.latitude == newItem.latitude
    }

    override fun areContentsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace): Boolean {
        return oldItem == newItem
    }
}