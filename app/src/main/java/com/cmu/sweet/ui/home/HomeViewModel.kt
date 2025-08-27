package com.cmu.sweet.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Para usar await com Tasks do Google Play Services

class HomeViewModel(
    // Injete o ApplicationContext se precisar dele para o FusedLocationProviderClient
    // ou outras operações que necessitem de contexto de aplicativo.
    // É melhor evitar passar Context diretamente se possível, ou usar um ApplicationContext.
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(applicationContext)

    init {
        checkInitialLocationPermission()
    }

    private fun checkInitialLocationPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.GRANTED) }
            // Se o mapa já estiver carregado (ou se não precisarmos esperar por ele para a primeira busca)
            // fetchUserLocation()
        } else {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.INITIAL) }
            // A UI solicitará a permissão
        }
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: () -> Boolean) {
        if (isGranted) {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.GRANTED) }
            if (_uiState.value.mapLoaded) { // Só busca se o mapa estiver pronto
                fetchUserLocation()
            }
        } else {
            if (shouldShowRationale()) {
                _uiState.update { it.copy(locationPermissionState = LocationPermissionState.NEEDS_RATIONALE) }
            } else {
                // Se não precisa de rationale, pode ser que o usuário negou permanentemente
                // ou é a primeira vez que nega (e não marcou "não perguntar novamente")
                // Você precisaria de uma lógica mais fina aqui para distinguir DENIED de PERMANENTLY_DENIED
                // Isso geralmente envolve verificar se é a primeira vez que se pede ou se shouldShowRequestPermissionRationale retorna false
                // após uma negação anterior.
                // Por simplicidade, vamos tratar como DENIED, mas você pode querer uma lógica para PERMANENTLY_DENIED.
                _uiState.update { it.copy(locationPermissionState = LocationPermissionState.DENIED) }
                // Idealmente, você teria uma forma de detectar PERMANENTLY_DENIED para mostrar um diálogo diferente
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (uiState.value.locationPermissionState != LocationPermissionState.GRANTED) {
            _uiState.update { it.copy(locationError = "Permissão de localização não concedida.") }
            return
        }

        _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }
        viewModelScope.launch {
            try {
                // Usar .await() requer kotlinx-coroutines-play-services
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    val newLatLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { currentState ->
                        currentState.copy(
                            userLocation = newLatLng,
                            isLoadingLocation = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoadingLocation = false, locationError = "Não foi possível obter a última localização conhecida.") }
                    // Poderia tentar buscar a localização atual aqui se lastLocation for null
                    // fusedLocationClient.getCurrentLocation(...)
                }
            } catch (e: Exception) {
                // Log.e("HomeViewModel", "Erro ao buscar localização", e)
                _uiState.update { it.copy(isLoadingLocation = false, locationError = "Erro ao buscar localização: ${e.message}") }
            }
        }
    }

    fun onMapLoaded() {
        _uiState.update { it.copy(mapLoaded = true) }
        // Se temos permissão e o mapa acabou de carregar, buscar localização
        if (uiState.value.locationPermissionState == LocationPermissionState.GRANTED) {
            fetchUserLocation()
        }
    }

    fun requestLocationPermissionAgain() {
        // Este método sinaliza para a UI que uma nova tentativa de permissão deve ser feita.
        // A UI, ao observar essa mudança (ou um evento específico), chamaria o locationPermissionLauncher.
        // Por exemplo, mudando o estado para algo que o LaunchedEffect na UI possa reagir.
        // Ou, a UI pode chamar diretamente o launcher e depois notificar o ViewModel com onPermissionResult.
        // Para este exemplo, vamos assumir que a UI chama o launcher e depois onPermissionResult.
        // Se a permissão está como DENIED ou NEEDS_RATIONALE, a UI pode tentar pedir novamente.
        // Se for PERMANENTLY_DENIED, a UI deve mostrar um diálogo para ir às configurações.
        // Vamos simplificar e assumir que a UI tentará pedir novamente se não for GRANTED.
        if (uiState.value.locationPermissionState != LocationPermissionState.GRANTED &&
            uiState.value.locationPermissionState != LocationPermissionState.PERMANENTLY_DENIED) {
            // A UI acionaria o locationPermissionLauncher
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.INITIAL) } // Para re-triggerar o pedido na UI
        } else if (uiState.value.locationPermissionState == LocationPermissionState.PERMANENTLY_DENIED) {
            _uiState.update { it.copy(showPermanentlyDeniedDialog = true) }
        }
    }

    fun onDismissPermissionRationaleDialog() {
        _uiState.update { it.copy(showPermissionRationaleDialog = false, locationPermissionState = LocationPermissionState.DENIED) }
        // Após mostrar o rationale, o estado volta para DENIED,
        // o usuário pode tentar novamente ou não.
    }

    fun onDismissPermanentlyDeniedDialog() {
        _uiState.update { it.copy(showPermanentlyDeniedDialog = false) }
    }

    fun onCenterMapOnUser() {
        if (uiState.value.locationPermissionState == LocationPermissionState.GRANTED) {
            if (uiState.value.mapLoaded) {
                // A UI usaria o userLocation do uiState para animar a câmera.
                // O ViewModel não precisa controlar a animação diretamente, apenas fornecer os dados.
                // Se a localização não foi obtida ainda, buscar.
                if (uiState.value.userLocation == HomeUiState().userLocation) { // Se ainda for o default de Lisboa
                    fetchUserLocation()
                }
                // A UI observará userLocation e mapLoaded para animar.
            }
        } else {
            // Sinalizar para a UI pedir permissão
            requestLocationPermissionAgain()
        }
    }

    // Adicionar funções para carregar estabelecimentos, etc.
    fun loadEstablishments() {
        _uiState.update { it.copy(isLoadingEstablishments = true) }
        viewModelScope.launch {
            // Simular carregamento
            kotlinx.coroutines.delay(2000)
            val exampleEstablishments = List(5) { index ->
                EstablishmentUiModel(
                    id = "$index",
                    name = "Pastelaria Modelo ${index + 1}",
                    location = LatLng(
                        uiState.value.userLocation.latitude + (Math.random() - 0.5) * 0.05,
                        uiState.value.userLocation.longitude + (Math.random() - 0.5) * 0.05
                    ),
                    rating = (3..5).random().toFloat(),
                    distance = "${(100..1500).random()}m"
                )
            }
            _uiState.update {
                it.copy(
                    establishments = exampleEstablishments,
                    isLoadingEstablishments = false
                )
            }
        }
    }
}
