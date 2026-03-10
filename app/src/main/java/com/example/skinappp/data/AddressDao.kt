package com.example.skinappp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.skinappp.model.SavedAddress
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Query("SELECT * from savedAddress")
    fun getAddr(): Flow<List<SavedAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(Sa : SavedAddress)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(Sa : SavedAddress)

    @Delete
    suspend fun delete(Sa : SavedAddress)

}