package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.SweetApplication
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.state.ProfileUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val establishmentRepository: EstablishmentRepository,
    private val reviewRepository: ReviewRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    val firestore = FirebaseFirestore.getInstance()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuário não autenticado.") }
                return@launch
            }

            userRepository.getProfile(currentUser.uid)
                .onSuccess { user ->
                    if (user != null) {
                        _uiState.update { it.copy(isLoading = false, user = user) }
                        // ← Call your helper here
                        loadUserEstablishments(user.id)
                        loadUserReviews(user.id)
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Perfil não encontrado.") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private suspend fun loadUserEstablishments(userId: String) {
        val userEstablishments = establishmentRepository.getByUser(userId)
        _uiState.update {
            it.copy(
                establishmentsAddedCount = userEstablishments.size,
                places = userEstablishments
            )
        }
    }

    private suspend fun loadUserReviews(userId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val result = reviewRepository.getByUserOnce(userId)

        result
            .onSuccess { reviews ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reviews = reviews,
                        reviewsCount = reviews.size
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Erro a carregar reviews"
                    )
                }
            }
    }

    fun attemptLogout(onLogoutConfirmed: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            try {
                userRepository.signOut()
                _uiState.value = ProfileUiState() // reset UI
                onLogoutConfirmed()
            } catch (e: Exception) {
                Timber.e(e, "Logout failed")
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

    class Factory(
        private val userRepository: UserRepository,
        private val application: Application,
        private val establishmentRepository: EstablishmentRepository,
        private val reviewRepository: ReviewRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(application, userRepository, establishmentRepository, reviewRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }



}
