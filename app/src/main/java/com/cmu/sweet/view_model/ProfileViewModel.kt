package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.SweetApplication
import com.cmu.sweet.data.auth.AuthManager
import com.cmu.sweet.data.local.repository.ProfileStore
import com.cmu.sweet.ui.state.ProfileUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sweetApp = application as SweetApplication
    private val userRepository: ProfileStore = sweetApp.profileStore
    private val authManager: AuthManager = sweetApp.authManager
    val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        Timber.Forest.d("loadUserProfile called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentFirebaseUser = authManager.getCurrentFirebaseAuthUser()
            if (currentFirebaseUser == null) {
                Timber.Forest.w("No authenticated user found. Cannot load profile.")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Utilizador não autenticado."
                    )
                }
                return@launch
            }
            Timber.Forest.d("Current user ID: ${currentFirebaseUser.uid}")

            userRepository.getProfile(currentFirebaseUser.uid)
                .onSuccess { userFromRepo -> // userFromRepo is of type User?
                    if (userFromRepo != null) {
                        Timber.Forest.d("User details loaded: ${userFromRepo.name}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = userFromRepo, // Now userFromRepo is User?
                            )
                        }
                        loadUserStats(currentFirebaseUser.uid)
                    } else {
                        Timber.Forest.w("User details not found for ID (via Result): ${currentFirebaseUser.uid}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Perfil não encontrado."
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    Timber.Forest.e(exception, "Error loading user profile from repository")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Erro ao carregar perfil: ${exception.message}"
                        )
                    }
                }
        }
    }


    private fun loadUserStats(userId: String) {
        viewModelScope.launch {
            // Simulate loading stats
            delay(500)
            _uiState.update { it.copy(reviewsCount = 10, establishmentsAddedCount = 3) }
            Timber.Forest.d("User stats loaded for $userId")
        }
    }


    fun attemptLogout(onLogoutConfirmed: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            try {
                delay(700)
                _uiState.value =
                    ProfileUiState(isLoading = false)
                onLogoutConfirmed()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        errorMessage = "Falha ao sair: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}