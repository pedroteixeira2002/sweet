package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.ui.state.EstablishmentDetails
import com.cmu.sweet.ui.state.EstablishmentDetailsUiState
import com.cmu.sweet.ui.state.ReviewUiModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EstablishmentDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val establishmentRepo: EstablishmentRepository,
    ) : ViewModel() {

    private val establishmentId: String = savedStateHandle["establishmentId"]
        ?: throw IllegalArgumentException("establishmentId não encontrado")

    private val _uiState = MutableStateFlow(EstablishmentDetailsUiState())
    val uiState: StateFlow<EstablishmentDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEstablishmentDetails()
        checkIfFavorite()
    }

    fun loadEstablishmentDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Pega o estabelecimento do Firestore
                val result = establishmentRepo.fetchById(establishmentId)

                result.fold(
                    onSuccess = { est ->
                        if (est != null) {
                            // Converte o modelo do repositório para seu UI model de detalhes
                            val details = EstablishmentDetails(
                                id = est.id,
                                name = est.name,
                                address = est.address,
                                rating = 0f,
                                location = LatLng(est.latitude, est.longitude),
                                description = est.description
                            )
                            _uiState.update { it.copy(establishment = details, isLoading = false) }
                        } else {
                            _uiState.update {
                                it.copy(isLoading = false, error = "Estabelecimento não encontrado.")
                            }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = "Falha ao carregar detalhes: ${e.message}")
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


    fun toggleFavorite() {
        // Lógica para adicionar/remover dos favoritos
        // viewModelScope.launch {
        //     val currentStatus = _uiState.value.isFavorite
        //     userRepository.setFavorite(establishmentId, !currentStatus)
        //     _uiState.update { it.copy(isFavorite = !currentStatus) }
        // }
        _uiState.update { it.copy(isFavorite = !it.isFavorite) } // Simulação
    }

    private fun checkIfFavorite() {
        // Lógica para verificar se já é favorito
        // viewModelScope.launch {
        //     val isFav = userRepository.isFavorite(establishmentId)
        //     _uiState.update { it.copy(isFavorite = isFav) }
        // }
    }

    fun retryLoadDetails() {
        loadEstablishmentDetails()
    }
}