package com.cmu.sweet.ui.state

import com.google.android.gms.maps.model.LatLng

/**
 * Estado da permissão de localização
 * - INITIAL: Estado inicial, ainda não verificado
 * - GRANTED: Permissão concedida
 * - DENIED: Permissão negada temporariamente
 * - NEEDS_RATIONALE: Precisa mostrar uma justificativa para o usuário
 * - PERMANENTLY_DENIED: Permissão negada permanentemente (usuário selecion ou "Não perguntar novamente")
 *
 */
enum class LocationPermissionState {
    INITIAL,
    GRANTED,
    DENIED,
    NEEDS_RATIONALE,
    PERMANENTLY_DENIED
}

/**
 * Estado da UI para a tela inicial
 * - userLocation: Localização atual do usuário (ou fallback)
 * - mapLoaded: Indica se o mapa foi carregado
 * - locationPermissionState: Estado da permissão de localização
 * - establishments: Lista de estabelecimentos para exibir no mapa
 * - isLoadingLocation: Indica se a localização está sendo carregada
 * - isLoadingEstablishments: Indica se os estabelecimentos estão sendo carregados
 * - locationError: Mensagem de erro de localização, se houver
 * - showPermissionRationaleDialog: Controla a exibição do diálogo de justificativa
 * - showPermanentlyDeniedDialog: Controla a exibição do diálogo de negação
 * - searchText: Texto atual na barra de pesquisa
 * - suggestions: Sugestões de pesquisa baseadas no texto atual
 * - selectedAddress: Endereço selecionado na pesquisa, se houver
 * - selectedLocation: Localização selecionada na pesquisa, se houver
 * - searchRadius: Raio de busca para filtrar estabelecimentos
 *
 */
data class HomeUiState(
    val userLocation: LatLng = LatLng(38.7169, -9.1393),
    val mapLoaded: Boolean = false,
    val locationPermissionState: LocationPermissionState = LocationPermissionState.INITIAL,
    val establishments: List<EstablishmentHomeUiState> = emptyList(),
    val isLoadingLocation: Boolean = false,
    val isLoadingEstablishments: Boolean = false,
    val locationError: String? = null,
    val showPermissionRationaleDialog: Boolean = false,
    val showPermanentlyDeniedDialog: Boolean = false,
    val searchText: String = "",
    val suggestions: List<String> = emptyList(),
    val selectedAddress: String? = null,
    val selectedLocation: LatLng? = null,
    val searchRadius: Float = 1000f,
)
