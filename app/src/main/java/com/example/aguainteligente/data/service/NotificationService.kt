package com.example.aguainteligente.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.aguainteligente.MainActivity
import com.example.aguainteligente.R

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showLeakAlertNotification() {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, LEAK_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("¡Alerta de Fuga!")
            .setContentText("Se ha detectado una posible fuga de agua en tu domicilio.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(LEAK_NOTIFICATION_ID, notification)
    }

    companion object {
        const val LEAK_NOTIFICATION_CHANNEL_ID = "leak_alerts"
        const val LEAK_NOTIFICATION_ID = 1
    }
}