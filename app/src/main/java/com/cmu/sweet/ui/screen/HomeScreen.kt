package com.cmu.sweet.ui.screen

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmu.sweet.data.local.repository.EstablishmentRepository
import com.cmu.sweet.data.local.repository.SweetDatabase
import com.cmu.sweet.ui.components.AddressSearchBar
import com.cmu.sweet.ui.components.AppBottomNavigationBar
import com.cmu.sweet.ui.components.EstablishmentCard
import com.cmu.sweet.ui.components.MiniFabItem
import com.cmu.sweet.ui.state.HomeUiState
import com.cmu.sweet.ui.state.LocationPermissionState
import com.cmu.sweet.ui.navigation.BottomNavItem
import com.cmu.sweet.view_model.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddEstablishment: () -> Unit,
    onNavigateToAddReview: (String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity
    var currentBottomNavItem by rememberSaveable(stateSaver = BottomNavItem.Saver) {
        mutableStateOf(BottomNavItem.Home)
    }
    val bottomNavItems =
        listOf(BottomNavItem.Home, BottomNavItem.Leaderboard, BottomNavItem.Profile)
    val homeContentUiState by homeViewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(homeContentUiState.userLocation, 10f)
    }
    var isFabExpanded by remember { mutableStateOf(false) }
    val fabIconRotation by animateFloatAsState(targetValue = if (isFabExpanded) 45f else 0f)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            homeViewModel.onPermissionResult(isGranted) {
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } ?: false
            }
        }
    )

    LaunchedEffect(
        currentBottomNavItem,
        homeContentUiState.locationPermissionState,
        homeContentUiState.mapLoaded
    ) {
        if (currentBottomNavItem == BottomNavItem.Home) {
            when (homeContentUiState.locationPermissionState) {
                LocationPermissionState.INITIAL -> {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        homeViewModel.updatePermissionState(LocationPermissionState.GRANTED)
                        if (homeContentUiState.mapLoaded) homeViewModel.fetchUserLocation()
                    }
                }

                LocationPermissionState.NEEDS_RATIONALE -> { /* Dialog will be shown */
                }

                LocationPermissionState.GRANTED -> {
                    if (homeContentUiState.mapLoaded) homeViewModel.fetchUserLocation()
                }

                LocationPermissionState.DENIED, LocationPermissionState.PERMANENTLY_DENIED -> { /* Dialogs will be shown */
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentBottomNavItem == BottomNavItem.Home) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                    ) {
                        MiniFabItem(
                            icon = Icons.Filled.RateReview, label = "Adicionar Review",
                            onClick = { isFabExpanded = false; onNavigateToAddReview(null) }
                        )
                    }
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                    ) {
                        MiniFabItem(
                            icon = Icons.Filled.Store, label = "Adicionar Sítio",
                            onClick = { isFabExpanded = false; onNavigateToAddEstablishment() }
                        )
                    }
                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (isFabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (isFabExpanded) "Fechar menu FAB" else "Abrir menu FAB",
                            modifier = Modifier.graphicsLayer(rotationZ = fabIconRotation)
                        )
                    }
                }
            }
        },
        bottomBar = {
            AppBottomNavigationBar(
                items = bottomNavItems,
                currentSelectedItem = currentBottomNavItem,
                onItemSelected = { selectedItem ->
                    currentBottomNavItem = selectedItem
                    isFabExpanded = false
                }
            )
        }
    ) { innerPadding ->
        if (currentBottomNavItem == BottomNavItem.Home && homeContentUiState.showPermissionRationaleDialog) {
            AlertDialog(
                onDismissRequest = { homeViewModel.onDismissPermissionRationaleDialog() },
                title = { Text("Permissão Necessária") },
                text = { Text("Precisamos da sua localização para mostrar estabelecimentos próximos e centralizar o mapa.") },
                confirmButton = {
                    Button(onClick = {
                        homeViewModel.onDismissPermissionRationaleDialog()
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }) { Text("Pedir Novamente") }
                },
                dismissButton = {
                    Button(onClick = { homeViewModel.onDismissPermissionRationaleDialog() }) {
                        Text(
                            "Cancelar"
                        )
                    }
                }
            )
        }
        if (currentBottomNavItem == BottomNavItem.Home && homeContentUiState.showPermanentlyDeniedDialog) {
            AlertDialog(
                onDismissRequest = { homeViewModel.onDismissPermanentlyDeniedDialog() },
                title = { Text("Permissão Negada Permanentemente") },
                text = { Text("A permissão de localização foi negada permanentemente. Para usar este recurso, você precisa habilitá-la nas configurações do aplicativo.") },
                confirmButton = {
                    Button(onClick = {
                        homeViewModel.onDismissPermanentlyDeniedDialog()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) { Text("Abrir Configurações") }
                },
                dismissButton = {
                    Button(onClick = { homeViewModel.onDismissPermanentlyDeniedDialog() }) {
                        Text(
                            "Fechar"
                        )
                    }
                }
            )
        }

        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentBottomNavItem) {
                BottomNavItem.Home -> HomeSectionContent(
                    homeViewModel = homeViewModel,
                    cameraPositionState = cameraPositionState,
                    uiState = homeContentUiState,
                    onNavigateToDetails = onNavigateToDetails
                )

                BottomNavItem.Leaderboard -> LeaderboardScreen(
                )

                BottomNavItem.Profile -> ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSectionContent(
    homeViewModel: HomeViewModel,
    cameraPositionState: CameraPositionState,
    uiState: HomeUiState,
    onNavigateToDetails: (establishmentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var searchRadius by remember { mutableStateOf(1000f) }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 64.dp,
        sheetContent = {
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(12.dp)) {
                Text("Estabelecimentos próximos", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Text("Raio: ${searchRadius.toInt()} metros")
                Slider(
                    value = searchRadius,
                    onValueChange = { searchRadius = it },
                    valueRange = 250f..20000f,
                    steps = 20
                )

                Spacer(Modifier.height(8.dp))

                if (uiState.isLoadingEstablishments && uiState.establishments.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.establishments.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum estabelecimento encontrado por perto.")
                    }
                } else {
                    LazyColumn {
                        items(uiState.establishments, key = { it.id }) { establishment ->
                            EstablishmentCard(
                                establishment = establishment,
                                onClick = { onNavigateToDetails(establishment.id) }
                            )
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
                uiState.establishments.forEach { establishment ->
                    Marker(
                        state = rememberUpdatedMarkerState(position = establishment.location),
                        title = establishment.name,
                        snippet = "Rating: ${establishment.rating} - ${establishment.distance}",
                        onInfoWindowClick = { onNavigateToDetails(establishment.id) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(0.95f)
            ) {

                DropdownMenu(
                    expanded = suggestions.isNotEmpty(),
                    onDismissRequest = { suggestions = emptyList() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    suggestions.forEach { prediction ->
                        DropdownMenuItem(
                            text = { Text(prediction.getFullText(null).toString()) },
                            onClick = {
                                suggestions = emptyList()
                                homeViewModel.selectSuggestion(prediction) { latLng: LatLng ->
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                    )
                                    homeViewModel.loadEstablishmentsNearby(
                                        center = latLng,
                                        radiusMeters = searchRadius.toDouble()
                                    )
                                }
                            }
                        )
                    }
                }
            }

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