package com.cmu.sweet

import android.app.Application
import com.google.android.libraries.places.api.Places
import timber.log.Timber
import kotlin.getValue


class SweetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAj5euiceTomJVILEJkuTTLgO43LnaDcCc")
        }
        Timber.plant(Timber.DebugTree())
        Timber.i("SweetApplication onCreate completed.")
    }
}