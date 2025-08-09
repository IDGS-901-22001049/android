package com.example.aguainteligente

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.aguainteligente.data.service.NotificationService
import com.google.firebase.FirebaseApp

class AguaInteligenteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationService.LEAK_NOTIFICATION_CHANNEL_ID,
                "Alertas de Fuga",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para fugas de agua detectadas."
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}