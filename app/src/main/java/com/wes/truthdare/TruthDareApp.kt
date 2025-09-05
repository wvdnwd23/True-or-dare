package com.wes.truthdare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TruthDareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application-wide initialization
    }
}