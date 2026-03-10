package com.example.skinappp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.skinappp.model.SavedAddress


@Database(entities = [SavedAddress::class],version =  1)
@TypeConverters(Converters::class )
abstract class AddressDatabase : RoomDatabase(){
    abstract fun addressDao(): AddressDao
}