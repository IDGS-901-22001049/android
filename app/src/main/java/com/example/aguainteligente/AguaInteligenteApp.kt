package com.example.aguainteligente

import android.app.Application
import com.google.firebase.FirebaseApp

class AguaInteligenteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}