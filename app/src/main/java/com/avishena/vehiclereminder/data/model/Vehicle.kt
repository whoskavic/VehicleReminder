package com.avishena.vehiclereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val currentOdometer: Int,
    val lastOilChangeDate: Long,
    val lastOilChangeOdometer: Int
)
