package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import com.cmu.sweet.ui.state.EstablishmentDetails
import com.cmu.sweet.ui.state.EstablishmentDetailsUiState
import com.cmu.sweet.ui.state.ReviewUiModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class EstablishmentDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    application: Application,
    private val establishmentRepo: EstablishmentRepository,
) : AndroidViewModel(application) {

    private val establishmentId: String =
        savedStateHandle.get<String>("establishmentId")
            ?: throw IllegalArgumentException("establishmentId não encontrado")

    private val _uiState = MutableStateFlow(EstablishmentDetailsUiState())
    val uiState: StateFlow<EstablishmentDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEstablishmentDetails()
    }

    fun loadEstablishmentDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = establishmentRepo.fetchById(establishmentId)

                result.fold(
                    onSuccess = { est ->
                        if (est != null) {
                            val details = EstablishmentDetails(
                                id = est.id,
                                name = est.name,
                                address = est.address,
                                rating = 0f,
                                location = LatLng(est.latitude, est.longitude),
                                description = est.description,
                                reviews = emptyList(),
                            )
                            _uiState.update { it.copy(establishment = details, isLoading = false) }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Estabelecimento não encontrado."
                                )
                            }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Falha ao carregar detalhes: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Falha ao carregar detalhes: ${e.message}")
                }
            }
        }
    }

    fun getEstablishmentReviews() {
        viewModelScope.launch {
            try {
                val result = establishmentRepo.getReviews(establishmentId)
                result.fold(
                    onSuccess = { reviews ->
                        val reviewUiModels = reviews.map { review ->
                            ReviewUiModel(
                                id = review.id,
                                userName = "Usuário ${review.userId.take(5)}",
                                date = review.timestamp.toString(),
                                rating = review.rating.toFloat(),
                                comment = review.comment,
                                photos = getPhotosForReview(review.id)
                            )
                        }

                        val currentEst = _uiState.value.establishment
                        if (currentEst != null) {
                            val updatedEst = currentEst.copy(reviews = reviewUiModels)
                            _uiState.update { it.copy(establishment = updatedEst) }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(error = "Falha ao carregar avaliações: ${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Falha ao carregar avaliações: ${e.message}")
                }
            }
        }
    }

    suspend fun getAverageRating(): Float? {
        return try {
            val reviews = establishmentRepo.getReviews(establishmentId).getOrNull()
            if (reviews.isNullOrEmpty()) {
                null
            } else {
                reviews.map { it.rating }.average().toFloat()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPhotosForReview(reviewId: String): List<String> {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("reviews/$reviewId")

        return try {
            // List all items in the review folder
            val result = storageRef.listAll().await()
            // Get download URLs for each item
            result.items.map { it.downloadUrl.await().toString() }
        } catch (e: Exception) {
            // Return empty list on failure
            emptyList()
        }
    }



    class Factory(
        private val application: Application,
        private val establishmentRepository: EstablishmentRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(EstablishmentDetailsViewModel::class.java)) {
                val savedStateHandle = extras.createSavedStateHandle()
                @Suppress("UNCHECKED_CAST")
                return EstablishmentDetailsViewModel(
                    savedStateHandle,
                    application,
                    establishmentRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}