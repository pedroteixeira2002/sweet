package com.cmu.sweet.ui.establishment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.ui.home.EstablishmentUiModel // Apenas para exemplo de dados mock
import com.google.android.gms.maps.model.LatLng // Apenas para exemplo de dados mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay // Para simular carregamento

class EstablishmentDetailsViewModel(
    savedStateHandle: SavedStateHandle
    // Injete seus repositórios/useCases aqui para buscar dados reais
    // private val establishmentRepository: EstablishmentRepository,
    // private val userRepository: UserRepository // Para verificar favoritos
) : ViewModel() {

    // O ID do estabelecimento é passado como argumento de navegação
    private val establishmentId: String = savedStateHandle.get<String>("establishmentId")
        ?: throw IllegalArgumentException("establishmentId não encontrado nos argumentos de navegação")

    private val _uiState = MutableStateFlow(EstablishmentDetailsUiState())
    val uiState: StateFlow<EstablishmentDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEstablishmentDetails()
        checkIfFavorite() // Exemplo
    }

    fun loadEstablishmentDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Simulação de busca de dados
                delay(1500) // Simula atraso da rede

                // Lógica real:
                // val details = establishmentRepository.getEstablishmentDetails(establishmentId)
                // _uiState.update { it.copy(establishment = details, isLoading = false) }

                // Dados Mock para exemplo:
                if (establishmentId == "0" || establishmentId == "1" || establishmentId == "2" || establishmentId == "3" || establishmentId == "4") { // IDs de exemplo da HomeScreen
                    val mockDetails = EstablishmentDetails(
                        id = establishmentId,
                        name = "Pastelaria Detalhada ${establishmentId.toInt() + 1}",
                        address = "Rua das Delícias, Nº ${100 + establishmentId.toInt()}, 1234-567 Cidade",
                        phoneNumber = "+351 210 000 00${establishmentId.toInt()}",
                        openingHours = listOf(
                            "Segunda a Sexta: 07:00 - 20:00",
                            "Sábado: 08:00 - 18:00",
                            "Domingo: Fechado"
                        ),
                        rating = (3.5f),
                        photos = listOf(
                            "https://picsum.photos/seed/${establishmentId}A/600/400", // Imagens de placeholder
                            "https://picsum.photos/seed/${establishmentId}B/600/400",
                            "https://picsum.photos/seed/${establishmentId}C/600/400"
                        ),
                        location = LatLng(38.7200 + (establishmentId.toInt() * 0.001), -9.1400 + (establishmentId.toInt() * 0.001)),
                        description = "Uma descrição maravilhosa desta pastelaria que oferece os melhores pastéis de nata da região. Venha provar as nossas especialidades e desfrutar de um ambiente acolhedor.",
                        reviews = List(3) { reviewIndex ->
                            ReviewUiModel(
                                id = "review_${establishmentId}_$reviewIndex",
                                userName = "Cliente Satisfeito ${reviewIndex + 1}",
                                userAvatarUrl = "https://i.pravatar.cc/100?u=user${establishmentId}${reviewIndex}",
                                rating = (4.0f),
                                comment = "Adorei os bolos! O atendimento também foi excelente. Recomendo vivamente!",
                                date = "Há ${ (1..5).random()} dias"
                            )
                        }
                    )
                    _uiState.update { it.copy(establishment = mockDetails, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Estabelecimento não encontrado.") }
                }

            } catch (e: Exception) {
                // Log.e("DetailsViewModel", "Erro ao carregar detalhes", e)
                _uiState.update { it.copy(isLoading = false, error = "Falha ao carregar detalhes: ${e.message}") }
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

