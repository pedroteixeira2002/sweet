package com.cmu.sweet.ui.auth

import FirebaseRepository
import UserRepository
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class SignUpViewModel(
    private val userRepository: UserRepository = UserRepository(FirebaseRepository())
) : ViewModel() {

    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun registerUser(
        name: String,
        email: String,
        password: String,
        profileImageUri: Uri?
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, registrationSuccess = false)

            val registrationResult: Result<FirebaseUser> = userRepository.registerUser(
                name = name.trim(),
                email = email.trim(),
                password = password,
                profileImageUri = profileImageUri
            )

            registrationResult.fold(
                onSuccess = { firebaseUser ->
                    uiState = uiState.copy(isLoading = false, registrationSuccess = true)
                },
                onFailure = { exception ->
                    uiState = uiState.copy(isLoading = false, error = exception.localizedMessage ?: "Erro no registo.")
                }
            )
        }
    }

    fun consumeError() {
        uiState = uiState.copy(error = null)
    }

    fun consumeRegistrationSuccess() {
        uiState = uiState.copy(registrationSuccess = false)
    }
}

