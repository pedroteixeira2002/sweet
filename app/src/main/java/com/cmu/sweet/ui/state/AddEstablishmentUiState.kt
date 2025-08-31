package com.cmu.sweet.ui.state

data class AddEstablishmentUiState(
    val name: String = "",
    val address: String = "",
    val type: String = "",
    val description: String = "",
    val addedBy: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)