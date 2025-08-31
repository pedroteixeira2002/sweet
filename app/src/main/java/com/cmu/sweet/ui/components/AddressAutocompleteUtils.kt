package com.cmu.sweet.ui.components

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Fetch address suggestions from Google Places API based on user input.
 * @param context Context needed to create Places client
 * @param query User input string to search for address suggestions
 * @return List of AutocompletePrediction objects
 *
 */
suspend fun fetchAddressSuggestions(
    placesClient: PlacesClient,
    query: String
): List<AutocompletePrediction> {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    return suspendCoroutine { cont ->
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                cont.resume(response.autocompletePredictions)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }
}


/**
 * Fetch the latitude and longitude of a place given its place ID.
 * @param context Context needed to create Places client
 * @param placeId The unique identifier of the place
 * @return LatLng object containing latitude and longitude, or null if not found
 *
 */
suspend fun fetchPlaceCoordinates(context: Context, placeId: String): LatLng? {
    val placesClient = Places.createClient(context)
    val request = FetchPlaceRequest.newInstance(
        placeId,
        listOf(Place.Field.LAT_LNG)
    )

    return try {
        val response = placesClient.fetchPlace(request).await()
        response.place.latLng
    } catch (e: Exception) {
        null
    }
}
