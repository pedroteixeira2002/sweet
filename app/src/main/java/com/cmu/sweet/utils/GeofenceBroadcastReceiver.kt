package com.cmu.sweet.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cmu.sweet.helpers.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber
import java.util.Calendar

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Geofence entered! Sending notification...")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        geofencingEvent?.let { event ->
            if (event.hasError()) return

            val geofenceTransition = event.geofenceTransition
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                val now = Calendar.getInstance()
                val hour = now.get(Calendar.HOUR_OF_DAY)
                if (hour in 16..19) {
                    NotificationHelper.notifyUser(
                        context,
                        "Restaurante próximo!",
                        "Você está a menos de 50 metros de um restaurante."
                    )
                }
            }
        }
    }
}
