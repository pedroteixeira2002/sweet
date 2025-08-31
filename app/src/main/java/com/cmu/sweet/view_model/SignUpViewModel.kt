package com.cmu.sweet.view_model

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.SweetApplication
import com.cmu.sweet.ui.state.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = (application as SweetApplication).authManager
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                nameInput = name,
                nameError = null,
                generalRegistrationError = null
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                emailInput = email,
                emailError = null,
                generalRegistrationError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                passwordInput = password,
                passwordError = null,
                generalRegistrationError = null
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPasswordInput = confirmPassword,
                confirmPasswordError = null,
                generalRegistrationError = null
            )
        }
    }

    fun attemptSignUp() {
        Timber.Forest.d("Attempting sign up with email: ${uiState.value.emailInput}")
        if (!validateInputs()) {
            Timber.Forest.w("Sign up validation failed.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    generalRegistrationError = null
                )
            } // Use generalRegistrationError
            val currentState = _uiState.value // Use the detailed state

            // Call AuthManager
            val result = authManager.registerUserWithProfile(
                name = currentState.nameInput.trim(),
                email = currentState.emailInput.trim(),
                password = currentState.passwordInput
            )

            result.fold(
                onSuccess = { firebaseUser ->
                    Timber.Forest.i("Registration successful for user: ${firebaseUser.uid}, email: ${firebaseUser.email}")
                    _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                },
                onFailure = { exception ->
                    Timber.Forest.e(
                        exception,
                        "Registration failed for email: ${currentState.emailInput}"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalRegistrationError = exception.message
                                ?: "Ocorreu um erro desconhecido." // Use generalRegistrationError
                        )
                    }
                }
            )
        }
    }

    // --- Add validation logic ---
    private fun validateInputs(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        if (currentState.nameInput.isBlank()) {
            _uiState.update { it.copy(nameError = "O nome não pode estar vazio.") }
            isValid = false
        } else {
            _uiState.update { it.copy(nameError = null) }
        }

        if (currentState.emailInput.isBlank()) {
            _uiState.update { it.copy(emailError = "O email não pode estar vazio.") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentState.emailInput).matches()) {
            _uiState.update { it.copy(emailError = "Formato de email inválido.") }
            isValid = false
        } else {
            _uiState.update { it.copy(emailError = null) }
        }

        if (currentState.passwordInput.isBlank()) {
            _uiState.update { it.copy(passwordError = "A senha não pode estar vazia.") }
            isValid = false
        } else if (currentState.passwordInput.length < 6) {
            _uiState.update { it.copy(passwordError = "A senha deve ter pelo menos 6 caracteres.") }
            isValid = false
        } else {
            _uiState.update { it.copy(passwordError = null) }
        }

        if (currentState.confirmPasswordInput != currentState.passwordInput) {
            _uiState.update { it.copy(confirmPasswordError = "As senhas não coincidem.") }
            isValid = false
        } else if (currentState.passwordInput.isNotBlank() && currentState.confirmPasswordInput.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Confirme a sua senha.") }
            isValid = false
        } else {
            _uiState.update { it.copy(confirmPasswordError = null) }
        }
        Timber.Forest.d("Input validation result: $isValid")
        return isValid
    }


    // --- Update consume methods ---
    fun onRegistrationCompleteNotified() { // Renamed from consumeRegistrationSuccess
        Timber.Forest.d("Registration complete event notified by UI.")
        // Reset general error as well
        _uiState.update { it.copy(registrationSuccess = false, generalRegistrationError = null) }
    }

    // Remove consumeError if generalRegistrationError is reset elsewhere or on input change
    // Or adapt it:
    fun clearGeneralError() {
        _uiState.update { it.copy(generalRegistrationError = null) }
    }
}