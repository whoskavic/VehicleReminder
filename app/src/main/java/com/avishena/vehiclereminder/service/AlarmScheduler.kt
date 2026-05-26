package com.avishena.vehiclereminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.WarmupAlarm
import com.avishena.vehiclereminder.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun scheduleWarmupAlarm(context: Context, alarm: WarmupAlarm, vehicleName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WARMUP
            putExtra(AlarmReceiver.EXTRA_VEHICLE_NAME, vehicleName)
            putExtra(AlarmReceiver.EXTRA_VEHICLE_ID, alarm.vehicleId)
            putExtra(AlarmReceiver.EXTRA_DAYS, alarm.days)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.vehicleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelWarmupAlarm(context: Context, vehicleId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WARMUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            vehicleId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun scheduleOilReminder(context: Context, reminder: OilReminder, vehicleName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_OIL
            putExtra(AlarmReceiver.EXTRA_VEHICLE_NAME, vehicleName)
            putExtra(AlarmReceiver.EXTRA_VEHICLE_ID, reminder.vehicleId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.vehicleId + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = if (reminder.nextTriggerTime > System.currentTimeMillis()) {
            reminder.nextTriggerTime
        } else {
            Calendar.getInstance().apply {
                add(Calendar.MONTH, reminder.intervalMonths)
            }.timeInMillis
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancelOilReminder(context: Context, vehicleId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_OIL
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            vehicleId + 10000,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
