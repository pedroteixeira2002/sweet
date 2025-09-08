package com.cmu.sweet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.libraries.places.api.Places
import timber.log.Timber
import kotlin.getValue


class SweetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("SweetApplication onCreate completed.")
    }
}