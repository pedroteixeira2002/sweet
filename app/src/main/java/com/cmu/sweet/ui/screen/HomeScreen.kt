package com.cmu.sweet.ui.screen

import android.Manifest
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.cmu.sweet.SweetApplication
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.components.AppBottomNavigationBar
import com.cmu.sweet.ui.components.MiniFabItem
import com.cmu.sweet.ui.state.LocationPermissionState
import com.cmu.sweet.ui.navigation.BottomNavItem
import com.cmu.sweet.view_model.HomeViewModel
import com.cmu.sweet.view_model.LeaderboardViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddEstablishment: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val activity = LocalActivity.current as? ComponentActivity
    val context = LocalContext.current

    val userDao = SweetDatabase.getInstance(context).userDao()
    val establishmentDao = SweetDatabase.getInstance(context).establishmentDao()
    val reviewDao = SweetDatabase.getInstance(context).reviewDao()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userRepository = UserRepository(firestore, userDao, firebaseAuth)
    val establishmentRepository = EstablishmentRepository(firestore, establishmentDao)
    val reviewRepository = ReviewRepository(firestore, reviewDao)
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

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentBottomNavItem) {
                BottomNavItem.Home -> HomeSectionContent(
                    homeViewModel = homeViewModel,
                    cameraPositionState = cameraPositionState,
                    uiState = homeContentUiState,
                    onNavigateToDetails = onNavigateToDetails,
                )

                BottomNavItem.Leaderboard -> LeaderboardScreen(
                    viewModel = viewModel(
                        factory = LeaderboardViewModel.Factory(
                            context.applicationContext as SweetApplication,
                            establishmentRepository,
                            reviewRepository
                        )
                    ),
                    onEstablishmentClick = onNavigateToDetails
                )

                BottomNavItem.Profile -> ProfileScreen(
                    userRepository = userRepository,
                    establishmentRepository = establishmentRepository,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    reviewRepository = reviewRepository,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}