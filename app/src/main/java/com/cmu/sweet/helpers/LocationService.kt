package com.cmu.sweet.helpers

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cmu.sweet.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    companion object {
        private var injectedLocationRepository: LocationRepository? = null
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        fun setLocationRepository(repository: LocationRepository) {
            injectedLocationRepository = repository
        }
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private var locationRepository: LocationRepository? = null
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        locationRepository = injectedLocationRepository
            ?: throw IllegalStateException("LocationRepository must be set before starting the service")
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand called") // Add log for onStartCommand
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        // Initialize the NotificationManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the initial notification
        notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        // Start the service in the foreground with the initial notification
        startForeground(1, notification.build())

        // Start location updates
        locationClient
            .getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                // Update the location in the repository
                locationRepository?.updateLocation(location)

                // Get the new location coordinates
                val lat = location.latitude.toString()
                val long = location.longitude.toString()

                // Update the notification text

            }
            .launchIn(serviceScope)
        notification.setContentText("Tracking location.")
        notificationManager.notify(1, notification.build())
    }
    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() //whenever service is destroyed, stop automatically tracking location
    }

}