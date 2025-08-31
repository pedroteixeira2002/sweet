package com.cmu.sweet.ui.state

data class SignUpUiState(
    // Input fields
    val nameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",

    // Validation errors for each field
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    // General state
    val isLoading: Boolean = false,
    val generalRegistrationError: String? = null, // For errors from AuthManager/ProfileStore
    val registrationSuccess: Boolean = false // To trigger navigation
)

