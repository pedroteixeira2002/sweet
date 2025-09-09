package com.cmu.sweet.ui.state

import com.google.android.gms.maps.model.LatLng

data class AddReviewUiState (
    val rating: Int = 0,
    val comment: String = "",
    val photoUrls: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val showCannotReviewDialog: Boolean = false,
    val userLocation: LatLng? = null,
    val locationError: String? = null
)