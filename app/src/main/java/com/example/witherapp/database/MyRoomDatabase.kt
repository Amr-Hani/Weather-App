package com.example.witherapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.witherapp.model.SingleAlarm
import com.example.witherapp.model.FavoritePlace

@Database(entities = arrayOf(FavoritePlace::class, SingleAlarm::class), version = 2)

abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun getAllFavoritePlace(): DAO

    companion object {
        private var instance: MyRoomDatabase? = null
        fun getInstance(context: Context): MyRoomDatabase {

            return instance ?: synchronized(this) {
                val temp = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                    "product_database"
                ).fallbackToDestructiveMigration()
                    .build()
                instance = temp
                temp
            }
        }
    }
    //  فائده ال synchronized هنا
    //1-Without synchronized: If two threads call getInstance() at the same time,
// both could potentially create separate instances of the MyDatabase, breaking the Singleton principle.
    //2-With synchronized: The block inside synchronized(this) ensures that only one thread can execute this block at any given time,
// forcing the other thread(s) to wait until the first one finishes creating the instance
// (if it hasn’t been created already).
}