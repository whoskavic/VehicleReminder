package com.avishena.vehiclereminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.service.AlarmScheduler
import com.avishena.vehiclereminder.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_WARMUP -> handleWarmup(context, intent)
            ACTION_OIL -> handleOil(context, intent)
        }
    }

    private fun handleWarmup(context: Context, intent: Intent) {
        val vehicleName = intent.getStringExtra(EXTRA_VEHICLE_NAME) ?: return
        val vehicleId = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)
        val days = intent.getStringExtra(EXTRA_DAYS) ?: return

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val scheduledDays = days.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (today !in scheduledDays) return

        NotificationHelper.showWarmupNotification(context, vehicleName, vehicleId)
    }

    private fun handleOil(context: Context, intent: Intent) {
        val vehicleName = intent.getStringExtra(EXTRA_VEHICLE_NAME) ?: return
        val vehicleId = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)
        if (vehicleId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationHelper.showOilReminderNotification(context, vehicleName, vehicleId + 10000)

                val dao = VehicleDatabase.getDatabase(context).vehicleDao()
                val reminder = dao.getOilReminder(vehicleId)
                if (reminder != null && reminder.isActive) {
                    val nextTrigger = Calendar.getInstance().apply {
                        add(Calendar.MONTH, reminder.intervalMonths)
                    }.timeInMillis
                    val updatedReminder = reminder.copy(nextTriggerTime = nextTrigger)
                    dao.insertOilReminder(updatedReminder)
                    AlarmScheduler.scheduleOilReminder(context, updatedReminder, vehicleName)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_WARMUP = "com.avishena.vehiclereminder.ACTION_WARMUP"
        const val ACTION_OIL = "com.avishena.vehiclereminder.ACTION_OIL"
        const val EXTRA_VEHICLE_NAME = "extra_vehicle_name"
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val EXTRA_DAYS = "extra_days"
    }
}
