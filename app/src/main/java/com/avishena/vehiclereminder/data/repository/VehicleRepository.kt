package com.avishena.vehiclereminder.data.repository

import androidx.lifecycle.LiveData
import com.avishena.vehiclereminder.data.db.VehicleDao
import com.avishena.vehiclereminder.data.model.OdometerHistory
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.model.WarmupAlarm

class VehicleRepository(private val dao: VehicleDao) {

    val allVehicles: LiveData<List<Vehicle>> = dao.getAllVehicles()

    fun getVehicleLive(vehicleId: Int): LiveData<Vehicle?> = dao.getVehicleLive(vehicleId)

    suspend fun insertVehicle(vehicle: Vehicle) = dao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = dao.updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: Vehicle) = dao.deleteVehicle(vehicle)

    suspend fun updateOdometer(vehicleId: Int, odometer: Int) = dao.updateOdometer(vehicleId, odometer)

    fun getOdometerHistory(vehicleId: Int): LiveData<List<OdometerHistory>> = dao.getOdometerHistory(vehicleId)

    suspend fun insertOdometerHistory(history: OdometerHistory) = dao.insertOdometerHistory(history)

    suspend fun deleteHistoryForVehicle(vehicleId: Int) = dao.deleteHistoryForVehicle(vehicleId)

    suspend fun getWarmupAlarm(vehicleId: Int) = dao.getWarmupAlarm(vehicleId)

    suspend fun insertWarmupAlarm(alarm: WarmupAlarm) = dao.insertWarmupAlarm(alarm)

    suspend fun deleteWarmupAlarm(alarm: WarmupAlarm) = dao.deleteWarmupAlarm(alarm)

    suspend fun getOilReminder(vehicleId: Int) = dao.getOilReminder(vehicleId)

    suspend fun insertOilReminder(reminder: OilReminder) = dao.insertOilReminder(reminder)

    suspend fun deleteOilReminder(reminder: OilReminder) = dao.deleteOilReminder(reminder)
}
