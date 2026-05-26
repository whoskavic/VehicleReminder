package com.avishena.vehiclereminder.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.avishena.vehiclereminder.data.model.OdometerHistory
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.model.WarmupAlarm

@Dao
interface VehicleDao {

    // ── Vehicle ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM vehicles ORDER BY name ASC")
    fun getAllVehicles(): LiveData<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleLive(id: Int): LiveData<Vehicle?>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET currentOdometer = :odometer WHERE id = :vehicleId")
    suspend fun updateOdometer(vehicleId: Int, odometer: Int)

    // ── OdometerHistory ───────────────────────────────────────────────────────

    @Query("SELECT * FROM odometer_history WHERE vehicleId = :vehicleId ORDER BY recordedAt DESC")
    fun getOdometerHistory(vehicleId: Int): LiveData<List<OdometerHistory>>

    @Insert
    suspend fun insertOdometerHistory(history: OdometerHistory)

    @Query("DELETE FROM odometer_history WHERE vehicleId = :vehicleId")
    suspend fun deleteHistoryForVehicle(vehicleId: Int)

    // ── WarmupAlarm ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM warmup_alarms WHERE vehicleId = :vehicleId LIMIT 1")
    suspend fun getWarmupAlarm(vehicleId: Int): WarmupAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarmupAlarm(alarm: WarmupAlarm): Long

    @Delete
    suspend fun deleteWarmupAlarm(alarm: WarmupAlarm)

    @Query("SELECT * FROM warmup_alarms WHERE isActive = 1")
    suspend fun getAllActiveWarmupAlarms(): List<WarmupAlarm>

    // ── OilReminder ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM oil_reminders WHERE vehicleId = :vehicleId LIMIT 1")
    suspend fun getOilReminder(vehicleId: Int): OilReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOilReminder(reminder: OilReminder): Long

    @Delete
    suspend fun deleteOilReminder(reminder: OilReminder)

    @Query("SELECT * FROM oil_reminders WHERE isActive = 1")
    suspend fun getAllActiveOilReminders(): List<OilReminder>
}
