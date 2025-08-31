package com.cmu.sweet.utils

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.tasks.await
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