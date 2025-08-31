package com.cmu.sweet.ui.state

import com.google.android.gms.maps.model.LatLng

data class EstablishmentHomeUiState(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val addedBy: String,
    val location: LatLng = LatLng(latitude, longitude),
    val distance: Double? = null,
    val rating: Float? = null
)