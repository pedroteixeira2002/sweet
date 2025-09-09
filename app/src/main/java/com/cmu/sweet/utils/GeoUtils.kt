package com.cmu.sweet.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.math.*

/**
 * Get latitude and longitude from a given address using Google Places API.
 * @param context Application context
 * @param address Address string
 * @return Pair of latitude and longitude
 * @throws Exception if address not found or coordinates not available
 *
 */
suspend fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double> {
    val placesClient = Places.createClient(context)
    val predictionsRequest = FindAutocompletePredictionsRequest.builder()
        .setQuery(address)
        .build()
    val predictionsResponse = placesClient.findAutocompletePredictions(predictionsRequest).await()
    val prediction = predictionsResponse.autocompletePredictions.firstOrNull()
        ?: throw Exception("Endereço não encontrado")
    val placeId = prediction.placeId
    val placeRequest = FetchPlaceRequest.builder(placeId, listOf(Place.Field.LAT_LNG)).build()
    val placeResponse = placesClient.fetchPlace(placeRequest).await()
    val latLng = placeResponse.place.latLng
        ?: throw Exception("Coordenadas não encontradas")
    return Pair(latLng.latitude, latLng.longitude)
}

/**
 * Calculate the Haversine distance between two points on the Earth.
 * @param lat1 Latitude of point 1
 * @param lon1 Longitude of point 1
 * @param lat2 Latitude of point 2
 * @param lon2 Longitude of point 2
 * @return Distance in meters
 *
 */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

fun distanceInMeters(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}

@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
fun addGeofence(context: Context, lat: Double, lng: Double, radius: Float) {
    val geofence = Geofence.Builder()
        .setRequestId("restaurant_$lat$lng")
        .setCircularRegion(lat, lng, radius) // raio em metros
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
        .addOnSuccessListener { Timber.d("Geofence added") }
        .addOnFailureListener { e -> Timber.e(e, "Failed to add geofence") }

}
