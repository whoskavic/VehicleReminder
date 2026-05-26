package com.avishena.vehiclereminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avishena.vehiclereminder.data.model.OdometerHistory
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.model.WarmupAlarm
import com.avishena.vehiclereminder.data.repository.VehicleRepository
import kotlinx.coroutines.launch

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    val allVehicles: LiveData<List<Vehicle>> = repository.allVehicles

    fun getVehicleLive(vehicleId: Int) = repository.getVehicleLive(vehicleId)

    fun insertVehicle(vehicle: Vehicle) = viewModelScope.launch {
        repository.insertVehicle(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle) = viewModelScope.launch {
        repository.deleteVehicle(vehicle)
        repository.deleteHistoryForVehicle(vehicle.id)
    }

    fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch {
        repository.updateVehicle(vehicle)
    }

    // ── Odometer ──────────────────────────────────────────────────────────────

    fun getOdometerHistory(vehicleId: Int): LiveData<List<OdometerHistory>> =
        repository.getOdometerHistory(vehicleId)

    fun addOdometerRecord(vehicleId: Int, newOdometer: Int) = viewModelScope.launch {
        repository.insertOdometerHistory(OdometerHistory(vehicleId = vehicleId, odometer = newOdometer))
        repository.updateOdometer(vehicleId, newOdometer)
    }

    fun markOilChanged(vehicle: Vehicle) = viewModelScope.launch {
        repository.updateVehicle(
            vehicle.copy(
                lastOilChangeOdometer = vehicle.currentOdometer,
                lastOilChangeDate = System.currentTimeMillis()
            )
        )
    }

    // ── WarmupAlarm ───────────────────────────────────────────────────────────

    suspend fun getWarmupAlarm(vehicleId: Int) = repository.getWarmupAlarm(vehicleId)

    fun saveWarmupAlarm(alarm: WarmupAlarm) = viewModelScope.launch {
        repository.insertWarmupAlarm(alarm)
    }

    fun deleteWarmupAlarm(alarm: WarmupAlarm) = viewModelScope.launch {
        repository.deleteWarmupAlarm(alarm)
    }

    // ── OilReminder ───────────────────────────────────────────────────────────

    suspend fun getOilReminder(vehicleId: Int) = repository.getOilReminder(vehicleId)

    fun saveOilReminder(reminder: OilReminder) = viewModelScope.launch {
        repository.insertOilReminder(reminder)
    }

    fun deleteOilReminder(reminder: OilReminder) = viewModelScope.launch {
        repository.deleteOilReminder(reminder)
    }
}
