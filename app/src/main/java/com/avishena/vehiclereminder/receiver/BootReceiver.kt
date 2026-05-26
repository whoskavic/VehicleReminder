package com.avishena.vehiclereminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = VehicleDatabase.getDatabase(context).vehicleDao()

                val warmupAlarms = dao.getAllActiveWarmupAlarms()
                warmupAlarms.forEach { alarm ->
                    val vehicle = dao.getVehicleById(alarm.vehicleId) ?: return@forEach
                    AlarmScheduler.scheduleWarmupAlarm(context, alarm, vehicle.name)
                }

                val oilReminders = dao.getAllActiveOilReminders()
                oilReminders.forEach { reminder ->
                    val vehicle = dao.getVehicleById(reminder.vehicleId) ?: return@forEach
                    AlarmScheduler.scheduleOilReminder(context, reminder, vehicle.name)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
