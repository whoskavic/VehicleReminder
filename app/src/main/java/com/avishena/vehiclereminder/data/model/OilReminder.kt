package com.avishena.vehiclereminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oil_reminders")
data class OilReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val intervalKm: Int = 0,
    val intervalMonths: Int = 3,
    val nextTriggerTime: Long = 0L,
    val isActive: Boolean = true
)
