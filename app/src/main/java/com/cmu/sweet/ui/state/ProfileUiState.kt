package com.cmu.sweet.ui.state

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val reviewsCount: Int = 0,
    val establishmentsAddedCount: Int = 0,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false
)