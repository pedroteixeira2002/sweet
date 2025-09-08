package com.cmu.sweet.ui.state

import com.google.android.gms.maps.model.LatLng

data class EstablishmentDetails(
    val id: String,
    val name: String,
    val address: String,
    val rating: Float,
    val location: LatLng,
    val description: String?,
    val reviews: List<ReviewUiModel>
)

data class ReviewUiModel(
    val id: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String,
    val photos: List<String>
)

data class EstablishmentDetailsUiState(
    val establishment: EstablishmentDetails? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false
)

