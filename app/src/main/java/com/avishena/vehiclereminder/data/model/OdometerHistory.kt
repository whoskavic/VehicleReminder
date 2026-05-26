package com.avishena.vehiclereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "odometer_history")
data class OdometerHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val odometer: Int,
    val recordedAt: Long = System.currentTimeMillis()
)
