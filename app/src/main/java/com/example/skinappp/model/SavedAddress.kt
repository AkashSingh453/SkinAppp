package com.example.skinappp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "savedAddress")
data class SavedAddress (
    @PrimaryKey(autoGenerate = false)
    var uuid : UUID = UUID.randomUUID(),

    var lat : Double,

    var lon : Double,

    var address : String,
)