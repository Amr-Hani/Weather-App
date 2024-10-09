package com.example.witherapp.ui.alarm.view

import androidx.recyclerview.widget.DiffUtil
import com.example.witherapp.model.SingleAlarm

class AlarmDiffUtil : DiffUtil.ItemCallback<SingleAlarm>() {
    override fun areItemsTheSame(oldItem: SingleAlarm, newItem: SingleAlarm): Boolean {
        return oldItem.primaryKey == newItem.primaryKey
    }

    override fun areContentsTheSame(oldItem: SingleAlarm, newItem: SingleAlarm): Boolean {
        return oldItem == newItem
    }
}