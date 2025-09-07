package com.cmu.sweet.ui.state

import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.local.entities.User

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
    val reviewsCount: Int = 0,
    val establishmentsAddedCount: Int = 0,
    val reviews: List<Review> = emptyList(),   // add this
    val places: List<Establishment> = emptyList()      // add this
)
