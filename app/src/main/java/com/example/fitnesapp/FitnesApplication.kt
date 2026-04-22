package com.example.fitnesapp

import android.app.Application
import com.example.fitnesapp.data.AppContainer

class FitnesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
