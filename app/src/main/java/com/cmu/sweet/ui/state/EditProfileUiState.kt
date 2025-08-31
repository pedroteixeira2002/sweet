package com.cmu.sweet.ui.state

data class EditProfileUiState(
    val isLoading: Boolean = true, // True when initially loading user data
    val isSaving: Boolean = false,  // True when save operation is in progress
    val userId: String = "",       // The ID of the user being edited

    // Form fields - initialize with current user's data or empty
    val nameInput: String = "",
    val emailDisplay: String = "", // Email is often not editable
    val bioInput: String = "",

    val initialName: String = "", // To check if data has changed
    val initialBio: String = "",

    val generalError: String? = null, // For errors during loading or saving
    val nameError: String? = null,    // For specific field validation errors
    // Add other field-specific errors if needed

    val isFormValid: Boolean = false, // True if current inputs are valid
    val hasChanges: Boolean = false,  // True if any field has changed from initial values
    val navigateBack: Boolean = false // Signal to navigate back after successful save or cancellation
)