package com.cmu.sweet.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.cmu.sweet.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()){
                throw LocationClient.LocationException("missing_location_permission_warning_english")
            }
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!isGpsEnabled && !isNetworkEnabled){
                throw LocationClient.LocationException("gps_is_disabled_english")
            }

            val request = LocationRequest.Builder(interval).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            // Request location updates
            client.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )

            // Ensure updates are stopped when the flow is closed
            awaitClose {
                removeLocationUpdates()
            }

        }
    }

    // Method to stop location tracking
    override fun removeLocationUpdates() {
        locationCallback?.let {
            client.removeLocationUpdates(it)
            locationCallback = null  // Reset the callback
        }
    }
}