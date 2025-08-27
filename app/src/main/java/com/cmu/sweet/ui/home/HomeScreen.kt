package com.cmu.sweet.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel // Import para viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // Injete o ViewModel. Se você estiver usando Hilt, será injetado automaticamente.
    // Caso contrário, você pode fornecer uma factory.
    // Para simplificar, vamos usar o `viewModel()` que requer `androidx.lifecycle:lifecycle-viewmodel-compose`.
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current.applicationContext)),
    onNavigateToDetails: (establishmentId: String) -> Unit
) {
    val context = LocalContext.current
    val activity =
        LocalActivity.current as? ComponentActivity // Para shouldShowRequestPermissionRationale

    // Observar o uiState do ViewModel
    val uiState by homeViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        // Inicializar com a localização do uiState (que tem o fallback de Lisboa)
        position = CameraPosition.fromLatLngZoom(uiState.userLocation, 10f)
    }

    // Função auxiliar para animar a câmera para uma LatLng
    val animateCameraToLocation: (LatLng, Float) -> Unit = { latLng, zoom ->
        // Garantir que o mapa esteja carregado antes de tentar animar
        if (uiState.mapLoaded) {
            coroutineScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(latLng, zoom),
                    1000 // duração da animação em ms
                )
            }
        }
    }

    // Observar mudanças na userLocation do uiState para animar o mapa
    // Apenas se não for a localização inicial padrão e o mapa estiver carregado
    LaunchedEffect(uiState.userLocation, uiState.mapLoaded) {
        if (uiState.mapLoaded && uiState.userLocation != HomeUiState().userLocation) {
            animateCameraToLocation(uiState.userLocation, 16f)
        }
    }
    // Efeito para carregar estabelecimentos quando a localização do usuário muda (e não é a padrão)
    // ou quando o mapa é carregado pela primeira vez com uma localização válida.
    LaunchedEffect(uiState.userLocation, uiState.mapLoaded) {
        if (uiState.mapLoaded && uiState.userLocation != HomeUiState().userLocation) {
            // Poderia adicionar uma lógica para não recarregar se a mudança de localização for mínima
            homeViewModel.loadEstablishments()
        }
    }


    // Launcher para solicitar permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            homeViewModel.onPermissionResult(isGranted) {
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } ?: false // Se activity for null, retorne false
            }
        }
    )

    // Efeito para lidar com o estado da permissão
    LaunchedEffect(uiState.locationPermissionState) {
        when (uiState.locationPermissionState) {
            LocationPermissionState.INITIAL, LocationPermissionState.NEEDS_RATIONALE -> {
                // Se NEEDS_RATIONALE, o ViewModel deve setar showPermissionRationaleDialog = true
                // Aqui só lançamos se for INITIAL ou se o diálogo de rationale for dispensado
                // e o ViewModel resetar o estado para permitir uma nova tentativa.
                if (uiState.locationPermissionState == LocationPermissionState.INITIAL &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            LocationPermissionState.GRANTED -> {
                if (uiState.mapLoaded) { // Se o mapa já estiver carregado
                    homeViewModel.fetchUserLocation()
                }
                // Se o mapa não estiver carregado, onMapLoaded() no ViewModel cuidará disso.
            }

            LocationPermissionState.DENIED -> {
                // O usuário negou, mas não permanentemente. Poderia mostrar uma mensagem sutil.
            }

            LocationPermissionState.PERMANENTLY_DENIED -> {
                // O ViewModel deve setar showPermanentlyDeniedDialog = true
            }
        }
    }

    // Diálogo para justificar a permissão
    if (uiState.showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { homeViewModel.onDismissPermissionRationaleDialog() },
            title = { Text("Permissão Necessária") },
            text = { Text("Precisamos da sua localização para mostrar estabelecimentos próximos e centralizar o mapa.") },
            confirmButton = {
                Button(onClick = {
                    homeViewModel.onDismissPermissionRationaleDialog() // Fecha o diálogo
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // Pede novamente
                }) { Text("Pedir Novamente") }
            },
            dismissButton = {
                Button(onClick = { homeViewModel.onDismissPermissionRationaleDialog() }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para permissão negada permanentemente
    if (uiState.showPermanentlyDeniedDialog) {
        AlertDialog(
            onDismissRequest = { homeViewModel.onDismissPermanentlyDeniedDialog() },
            title = { Text("Permissão Negada Permanentemente") },
            text = { Text("A permissão de localização foi negada permanentemente. Para usar este recurso, você precisa habilitá-la nas configurações do aplicativo.") },
            confirmButton = {
                Button(onClick = {
                    homeViewModel.onDismissPermanentlyDeniedDialog()
                    // Abrir configurações do app
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }) { Text("Abrir Configurações") }
            },
            dismissButton = {
                Button(onClick = { homeViewModel.onDismissPermanentlyDeniedDialog() }) { Text("Fechar") }
            }
        )
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                homeViewModel.onCenterMapOnUser()
                // A lógica de animar ou pedir permissão agora está no ViewModel/LaunchedEffects
                // Apenas precisamos garantir que a localização do usuário no uiState seja atualizada
                // e o LaunchedEffect(uiState.userLocation, uiState.mapLoaded) fará a animação.
            }) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Minha Localização")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true),
                    onMapLoaded = {
                        homeViewModel.onMapLoaded()
                    },
                    // Mostrar progresso enquanto o mapa ou localização estão carregando
                    // (Exemplo simples, pode ser mais granular)
                    properties = MapProperties(
                        isMyLocationEnabled = uiState.locationPermissionState == LocationPermissionState.GRANTED && uiState.userLocation != HomeUiState().userLocation
                        // Cuidado: isMyLocationEnabled mostra o ponto azul padrão do Google Maps.
                        // Se você já tem seu próprio Marker, pode não precisar dele.
                    )
                ) {
                    // Marcador para a localização do usuário
                    // Só mostra se a localização não for a padrão de Lisboa (ou seja, foi obtida)
                    if (uiState.userLocation != HomeUiState().userLocation) {
                        Marker(
                            state = rememberMarkerState(position = uiState.userLocation),
                            title = "Você está aqui 🍰"
                            // snippet = "Lat: ${uiState.userLocation.latitude}, Lng: ${uiState.userLocation.longitude}"
                        )
                    }

                    // Marcadores para estabelecimentos
                    uiState.establishments.forEach { establishment ->
                        Marker(
                            state = rememberMarkerState(position = establishment.location),
                            title = establishment.name,
                            snippet = "Rating: ${establishment.rating} - ${establishment.distance}",
                            onInfoWindowClick = { // Ou no clique do marcador se preferir
                                onNavigateToDetails(establishment.id)
                            },
                            onClick = {
                                // Você pode querer que o clique no marcador também navegue
                                // ou apenas mostre a InfoWindow. Se onInfoWindowClick for usado,
                                // o onClick pode retornar false para permitir o comportamento padrão da InfoWindow.
                                // Se quiser navegação imediata no clique do marcador:
                                // onNavigateToDetails(establishment.id)
                                false // Deixa a InfoWindow aparecer
                            }
                        )
                    }
                }

                if (uiState.isLoadingLocation || (uiState.isLoadingEstablishments && uiState.establishments.isEmpty())) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.locationError?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        action = { Button(onClick = { /* ViewModel.clearError() */ }) { Text("OK") } }
                    ) { Text(error) }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f) // Dar peso 1 para a lista também
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text("Estabelecimentos próximos", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                if (uiState.isLoadingEstablishments && uiState.establishments.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.establishments.isEmpty() && !uiState.isLoadingEstablishments) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum estabelecimento encontrado por perto.")
                    }
                } else {
                    LazyColumn {
                        items(uiState.establishments, key = { it.id }) { establishment ->
                            EstablishmentCard(
                                establishment = establishment,
                                onClick = {
                                    onNavigateToDetails(establishment.id) // NAVEGAR AO CLICAR NO CARTÃO
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstablishmentCard(
    establishment: EstablishmentUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large
        // onClick = { /* viewModel.onEstablishmentCardClicked(establishment.id) */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(establishment.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "⭐ ${establishment.rating} • ${establishment.distance}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Factory simples para o HomeViewModel se não estiver usando Hilt
class HomeViewModelFactory(private val applicationContext: Context) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
