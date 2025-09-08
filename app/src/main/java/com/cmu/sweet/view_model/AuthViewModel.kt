package com.cmu.sweet.view_model

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.local.dao.UserDao
import com.cmu.sweet.data.local.entities.User
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.state.SignUpUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthViewModel(
    application: Application,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AndroidViewModel(application) {

    private val repository = UserRepository(firestore, userDao, firebaseAuth)

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
        Timber.d("Attempting sign up with email: ${uiState.value.emailInput}")
        if (!validateInputs()) return

        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalRegistrationError = null) }

            try {
                // 1️⃣ Register user with Firebase Auth
                val authResult = firebaseAuth.createUserWithEmailAndPassword(
                    currentState.emailInput.trim(),
                    currentState.passwordInput
                ).await()
                val firebaseUser =
                    authResult.user ?: throw Exception("Firebase user creation failed.")

                // 2️⃣ Create User entity
                val newUser = User(
                    id = firebaseUser.uid,
                    name = currentState.nameInput.trim(),
                    email = currentState.emailInput.trim()
                )

                // 3️⃣ Save user remotely & locally via repository
                repository.add(newUser)

                Timber.i("Registration successful for user: ${firebaseUser.uid}")
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }

            } catch (e: Exception) {
                Timber.e(e, "Registration failed for email: ${currentState.emailInput}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalRegistrationError = e.message ?: "Ocorreu um erro desconhecido."
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        var valid = true

        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(nameError = "O nome não pode estar vazio.") }; valid = false
        }
        if (state.emailInput.isBlank()) {
            _uiState.update { it.copy(emailError = "O email não pode estar vazio.") }; valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.emailInput).matches()) {
            _uiState.update { it.copy(emailError = "Formato de email inválido.") }; valid = false
        }

        if (state.passwordInput.isBlank()) {
            _uiState.update { it.copy(passwordError = "A senha não pode estar vazia.") }; valid =
                false
        } else if (state.passwordInput.length < 6) {
            _uiState.update { it.copy(passwordError = "A senha deve ter pelo menos 6 caracteres.") }; valid =
                false
        }

        if (state.confirmPasswordInput != state.passwordInput) {
            _uiState.update { it.copy(confirmPasswordError = "As senhas não coincidem.") }; valid =
                false
        } else if (state.passwordInput.isNotBlank() && state.confirmPasswordInput.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Confirme a sua senha.") }; valid =
                false
        }

        Timber.d("Input validation result: $valid")
        return valid
    }

    fun onRegistrationCompleteNotified() {
        _uiState.update { it.copy(registrationSuccess = false, generalRegistrationError = null) }
    }

    fun clearGeneralError() {
        _uiState.update { it.copy(generalRegistrationError = null) }
    }

    class Factory(
        private val application: Application,
        private val userDao: UserDao,
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(application, userDao, firestore, firebaseAuth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

}
