package com.cmu.sweet.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cmu.sweet.ui.components.AddressSearchBar
import com.cmu.sweet.ui.components.EstablishmentCard
import com.cmu.sweet.ui.state.HomeUiState
import com.cmu.sweet.ui.state.LocationPermissionState
import com.cmu.sweet.view_model.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSectionContent(
    homeViewModel: HomeViewModel,
    cameraPositionState: CameraPositionState,
    uiState: HomeUiState,
    onNavigateToDetails: (establishmentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var searchQuery by remember { mutableStateOf("") }
    val suggestions = uiState.suggestions
    var searchRadius by remember { mutableFloatStateOf(1000f) }
    val scope = rememberCoroutineScope()

    // Carrega establishments automaticamente quando a localização estiver disponível
    LaunchedEffect(uiState.userLocation, searchRadius) {
        uiState.userLocation?.let { location ->
            homeViewModel.loadEstablishmentsNearby(location, searchRadius.toDouble())
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 64.dp,
        sheetContent = {
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(12.dp)) {
                Text("Estabelecimentos próximos", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Distância: ${
                        if (searchRadius < 1000) "${searchRadius.toInt()} m"
                        else String.format("%.1f km", searchRadius / 1000)
                    }"
                )
                Slider(
                    value = searchRadius,
                    onValueChange = { searchRadius = it },
                    valueRange = 250f..15000f,
                    steps = 60
                )

                Spacer(Modifier.height(8.dp))

                when {
                    uiState.isLoadingEstablishments && uiState.establishments.isEmpty() -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.establishments.isEmpty() -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Nenhum estabelecimento encontrado por perto.")
                        }
                    }
                    else -> {
                        LazyColumn {
                            items(uiState.establishments, key = { it.id }) { establishment ->
                                EstablishmentCard(
                                    establishment = establishment,
                                    onClick = {
                                        // Corrige a rota para bater com NavGraph
                                        onNavigateToDetails(establishment.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapLoaded = { homeViewModel.onMapLoaded() },
                properties = MapProperties(
                    isMyLocationEnabled = uiState.locationPermissionState == LocationPermissionState.GRANTED
                )
            ) {
                uiState.userLocation?.let { location ->
                    LaunchedEffect(location) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(location, 15f),
                            durationMs = 1000
                        )
                    }
                }

                uiState.establishments.forEach { est ->
                    Marker(
                        state = rememberUpdatedMarkerState(position = est.location),
                        title = est.name,
                        snippet = "Rating: ${est.rating ?: "N/A"} - ${est.distance?.toInt() ?: "?"} m",
                        onInfoWindowClick = {
                            // Navega para details usando a rota correta
                            onNavigateToDetails(est.id)
                        }
                    )
                }
            }

            AddressSearchBar(
                searchText = searchQuery,
                onSearchTextChange = { query ->
                    searchQuery = query
                    scope.launch {
                        delay(400) // debounce
                        homeViewModel.searchAddress(query)
                    }
                },
                suggestions = suggestions,
                onSuggestionClick = { prediction ->
                    searchQuery = prediction.getFullText(null).toString()

                    homeViewModel.selectSuggestion(prediction) { latLng ->
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                durationMs = 1000
                            )
                        }
                        homeViewModel.loadEstablishmentsNearby(latLng, searchRadius.toDouble())
                    }
                    homeViewModel.clearSuggestions()
                }
            )

            if (uiState.isLoadingLocation) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            uiState.locationError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    action = {
                        Button(onClick = { homeViewModel.clearLocationError() }) { Text("OK") }
                    }
                ) { Text(error) }
            }
        }
    }
}
