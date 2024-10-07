package com.example.witherapp.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.example.witherapp.model.WitherForecastItem

class DiffUtil : DiffUtil.ItemCallback<WitherForecastItem>() {
    override fun areItemsTheSame(
        oldItem: WitherForecastItem,
        newItem: WitherForecastItem
    ): Boolean {
        return oldItem.dt_txt == newItem.dt_txt
    }

    override fun areContentsTheSame(
        oldItem: WitherForecastItem,
        newItem: WitherForecastItem
    ): Boolean {
        return oldItem == newItem
    }

}