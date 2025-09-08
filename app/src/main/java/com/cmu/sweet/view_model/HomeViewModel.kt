package com.cmu.sweet.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.ui.components.fetchAddressSuggestions
import com.cmu.sweet.ui.components.fetchPlaceCoordinates
import com.cmu.sweet.ui.state.EstablishmentHomeUiState
import com.cmu.sweet.ui.state.HomeUiState
import com.cmu.sweet.ui.state.LocationPermissionState
import com.cmu.sweet.utils.haversineDistance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val dao = SweetDatabase.getInstance(application).establishmentDao()
    private val repository = EstablishmentRepository(firestore, dao)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private val appContext = application.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
    private val placesClient by lazy {
        Places.createClient(appContext)
    }

    init {
        checkInitialLocationPermission()
    }

    private fun checkInitialLocationPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.GRANTED) }
            // Não chamar fetchUserLocation() aqui diretamente.
            // A UI (HomeScreen/HomeSectionContent) verificará mapLoaded e locationPermissionState
            // e então chamará fetchUserLocation ou o ViewModel o fará em onMapLoaded.
        } else {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.INITIAL) }
        }
    }

    fun updatePermissionState(newState: LocationPermissionState) {
        _uiState.update { it.copy(locationPermissionState = newState) }
        // Se o novo estado for GRANTED e o mapa estiver carregado, tentar buscar a localização
        if (newState == LocationPermissionState.GRANTED && _uiState.value.mapLoaded) {
            fetchUserLocation()
        }
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationaleProvider: () -> Boolean) {
        if (isGranted) {
            _uiState.update { it.copy(locationPermissionState = LocationPermissionState.GRANTED) }
            if (_uiState.value.mapLoaded) {
                fetchUserLocation()
            }
        } else {
            // A função shouldShowRationaleProvider() deve ser chamada pela UI ANTES de pedir a permissão
            // pela segunda vez. Se ela retorna true, mostramos o rationale.
            // Se retorna false APÓS uma negação, pode ser uma negação permanente.
            // O ActivityResultContracts.RequestPermission não nos dá diretamente o "não perguntar novamente".
            // A lógica de shouldShowRationaleProvider é mais útil para a UI decidir se mostra o rationale
            // antes de pedir novamente.
            if (shouldShowRationaleProvider()) {
                _uiState.update {
                    it.copy(
                        locationPermissionState = LocationPermissionState.NEEDS_RATIONALE,
                        showPermissionRationaleDialog = true
                    )
                }
            } else {
                // Se não precisa de rationale, pode ser a primeira negação (sem marcar "não perguntar")
                // ou uma negação permanente. A UI, ao tentar pedir novamente e shouldShowRationaleProvider ainda
                // retornar false, pode inferir negação permanente.
                // Por agora, o requestLocationPermissionAgain tratará a lógica de PERMANENTLY_DENIED.
                _uiState.update { it.copy(locationPermissionState = LocationPermissionState.DENIED) }
                // Vamos acionar a verificação para o diálogo de negação permanente aqui,
                // já que o usuário acabou de negar e não quer ver o rationale.
                // Isso é uma simplificação; uma detecção mais robusta de "don't ask again" é complexa.
                // Se a UI tentar pedir novamente e shouldShowRationale ainda for false, aí sim é mais certo.
                // Vamos assumir que se o rationale não é necessário após uma negação, mostramos o diálogo de permanente.
                // Esta lógica pode precisar de ajuste fino baseado no comportamento exato desejado.
                // Uma maneira mais simples: se negado e shouldShowRationaleProvider() é false,
                // pode ser que o usuário nunca tenha sido perguntado antes OU negou permanentemente.
                // O estado DENIED é um bom fallback. A UI pode tentar chamar requestLocationPermissionAgain()
                // e essa função verificará o estado.
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (uiState.value.locationPermissionState != LocationPermissionState.GRANTED) {
            Timber.tag("HomeViewModel").w("fetchUserLocation chamado sem permissão GRANTED.")
            return
        }
        if (!_uiState.value.mapLoaded) {
            Timber.tag("HomeViewModel")
                .i("fetchUserLocation chamado, mas mapa não está carregado ainda.")
            return
        }

        _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }
        viewModelScope.launch {
            try {
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
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            locationError = "Não foi possível obter a última localização conhecida."
                        )
                    }
                }
            } catch (e: SecurityException) {
                Timber.tag("HomeViewModel").e(e, "SecurityException ao buscar localização")
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        locationError = "Permissão de localização revogada ou ausente.",
                        locationPermissionState = LocationPermissionState.DENIED
                    )
                }
            } catch (e: Exception) {
                Timber.tag("HomeViewModel").e(e, "Erro ao buscar localização")
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        locationError = "Erro ao buscar localização: ${e.message}"
                    )
                }
            }
        }
    }

    fun onMapLoaded() {
        val wasMapAlreadyLoaded = _uiState.value.mapLoaded
        _uiState.update { it.copy(mapLoaded = true) }
        if (!wasMapAlreadyLoaded && uiState.value.locationPermissionState == LocationPermissionState.GRANTED) {
            fetchUserLocation()
        }
    }

    fun onDismissPermissionRationaleDialog() {
        _uiState.update { it.copy(showPermissionRationaleDialog = false) }
        }

    fun onDismissPermanentlyDeniedDialog() {
        _uiState.update { it.copy(showPermanentlyDeniedDialog = false) }
    }

    fun clearLocationError() {
        _uiState.update { it.copy(locationError = null) }
    }

    fun selectSuggestion(
        prediction: AutocompletePrediction,
        onLocationFetched: (LatLng) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val placeCoordinates = fetchPlaceCoordinates(
                    getApplication(),
                    prediction.placeId
                )
                placeCoordinates?.let { latLng ->
                    onLocationFetched(latLng)
                }
                _uiState.update { it.copy(suggestions = emptyList()) }

            } catch (e: Exception) {
                _uiState.update { it.copy(locationError = "Erro ao obter coordenadas: ${e.message}") }
            }
        }
    }

    fun searchAddress(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            try {
                val suggestions = fetchAddressSuggestions(placesClient, query)
                _uiState.update { it.copy(suggestions = suggestions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(searchError = e.message) }
            }
        }
    }

    fun updateSuggestions(predictions: List<AutocompletePrediction>) {
        _uiState.update { it.copy(suggestions = predictions) }
    }

    fun loadEstablishmentsNearby(center: LatLng, radiusMeters: Double) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingEstablishments = true,
                    establishments = emptyList()
                )
            }
            try {
                val nearbyEntities = repository.fetchNearbyEstablishments(
                    center.latitude,
                    center.longitude,
                    radiusMeters
                )

                Timber.d("Fetched ${nearbyEntities.size} establishments from Firestore")

                val uiModels = nearbyEntities.map { establishment ->
                    val distance = haversineDistance(
                        center.latitude,
                        center.longitude,
                        establishment.latitude,
                        establishment.longitude
                    )
                    Timber.d(
                        "Entity: ${establishment.name} | LatLng=(${establishment.latitude}, ${establishment.longitude}) | Distance=${
                            "%.2f".format(
                                distance
                            )
                        } m"
                    )

                    EstablishmentHomeUiState(
                        id = establishment.id,
                        name = establishment.name,
                        address = establishment.address,
                        latitude = establishment.latitude,
                        longitude = establishment.longitude,
                        distance = distance,
                        rating = repository.getRating(establishment.id).getOrNull(),
                        type = establishment.type,
                        description = establishment.description,
                        addedBy = establishment.addedBy
                    )
                }
                    .filter { it.distance!! <= radiusMeters } // filter by radius
                    .sortedBy { it.distance } // sort closest first

                Timber.d("After filtering: ${uiModels.size} establishments within ${radiusMeters}m")

                _uiState.update {
                    it.copy(
                        establishments = uiModels,
                        isLoadingEstablishments = false,
                        locationError = null
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error loading establishments nearby")
                _uiState.update {
                    it.copy(
                        isLoadingEstablishments = false,
                        locationError = "Erro ao carregar estabelecimentos: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList(), isDropdownExpanded = false) }
    }


}