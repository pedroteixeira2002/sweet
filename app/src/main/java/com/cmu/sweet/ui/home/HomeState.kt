package com.cmu.sweet.ui.home

import com.google.android.gms.maps.model.LatLng

// Representa um estabelecimento individual na UI
data class EstablishmentUiModel( // Renomeado de Establishment para evitar conflito com o modelo de domínio
    val id: String,
    val name: String,
    val location: LatLng,
    val rating: Float,
    val distance: String,
    // val imageUrl: String? = null // Se aplicável
)

// Representa os diferentes estados da permissão de localização
enum class LocationPermissionState {
    INITIAL, // Estado antes de qualquer verificação
    GRANTED,
    DENIED, // Negada, mas pode pedir novamente
    NEEDS_RATIONALE, // Precisa mostrar justificação antes de pedir novamente
    PERMANENTLY_DENIED // Negada e "Não perguntar novamente" foi selecionado
}

// Agrega todo o estado relevante para a HomeScreen
data class HomeUiState(
    val userLocation: LatLng = LatLng(38.7169, -9.1393), // Lisboa como fallback inicial
    val mapLoaded: Boolean = false,
    val locationPermissionState: LocationPermissionState = LocationPermissionState.INITIAL,
    val establishments: List<EstablishmentUiModel> = emptyList(),
    val isLoadingLocation: Boolean = false, // Para mostrar um loader específico para localização
    val isLoadingEstablishments: Boolean = false, // Para mostrar um loader para estabelecimentos
    val locationError: String? = null, // Mensagem de erro de localização, se houver
    val showPermissionRationaleDialog: Boolean = false, // Para controlar a exibição do diálogo de justificação
    val showPermanentlyDeniedDialog: Boolean = false, // Para controlar o diálogo de negação permanente
)
