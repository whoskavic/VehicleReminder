package com.avishena.vehiclereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warmup_alarms")
data class WarmupAlarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val hour: Int,
    val minute: Int,
    val days: String, // comma-separated Calendar.DAY_OF_WEEK values, e.g. "2,3,4,5,6"
    val isActive: Boolean = true
)
