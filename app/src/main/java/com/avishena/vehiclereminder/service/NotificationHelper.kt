package com.avishena.vehiclereminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.avishena.vehiclereminder.R

object NotificationHelper {

    const val WARMUP_CHANNEL_ID = "warmup_channel"
    const val OIL_CHANNEL_ID = "oil_channel"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val warmupChannel = NotificationChannel(
            WARMUP_CHANNEL_ID,
            "Reminder Panasin Kendaraan",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi pengingat untuk memanaskan kendaraan"
        }

        val oilChannel = NotificationChannel(
            OIL_CHANNEL_ID,
            "Reminder Ganti Oli",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi pengingat penggantian oli kendaraan"
        }

        manager.createNotificationChannel(warmupChannel)
        manager.createNotificationChannel(oilChannel)
    }

    fun showWarmupNotification(context: Context, vehicleName: String, notifId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, WARMUP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Waktunya Panasin Kendaraan!")
            .setContentText("Jangan lupa panaskan $vehicleName Anda sekarang.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(notifId, notification)
    }

    fun showOilReminderNotification(context: Context, vehicleName: String, notifId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, OIL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Waktunya Ganti Oli!")
            .setContentText("Saatnya mengganti oli $vehicleName Anda.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(notifId, notification)
    }
}
