package com.cmu.sweet.ui.state

data class AddReviewUiState (
    val rating: Int = 0,
    val comment: String = "",
    val photoUrls: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val success: Boolean = false
)