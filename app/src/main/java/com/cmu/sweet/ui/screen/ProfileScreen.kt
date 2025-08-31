package com.cmu.sweet.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmu.sweet.view_model.ProfileViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (userId: String) -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                actions = {
                    uiState.user?.let {
                        IconButton(onClick = { onNavigateToEditProfile(it.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Perfil")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.user == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Oops! ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { profileViewModel.loadUserProfile() }) {
                        Text("Tentar Novamente")
                    }
                }
            } else {
                uiState.user?.let { user ->
                    ProfileContent(
                        user = user,
                        reviewsCount = uiState.reviewsCount,
                        establishmentsAddedCount = uiState.establishmentsAddedCount,
                        onLogoutClicked = { showLogoutDialog = true }
                    )
                }
            }

            if (uiState.isLoggingOut) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirmar Saída") },
            text = { Text("Tem a certeza que deseja sair da sua conta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        profileViewModel.attemptLogout {
                            onNavigateToLogin()
                        }
                    },
                    enabled = !uiState.isLoggingOut
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    user: User,
    reviewsCount: Int,
    establishmentsAddedCount: Int,
    reviews: List<Review> = emptyList(), // Adicione este parâmetro
    places: List<Place> = emptyList(),   // Adicione este parâmetro
    onLogoutClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        user.name?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        user.email?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }
        user.bio?.let { // Exibe a bio se existir
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(label = "Avaliações", value = reviewsCount.toString())
            ProfileStat(label = "Sítios Adicionados", value = establishmentsAddedCount.toString())
        }

        // Lista de avaliações
        if (reviews.isNotEmpty()) {
            Text("Minhas Avaliações", style = MaterialTheme.typography.titleMedium)
            reviews.forEach { review ->
                Text("- $review", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Lista de lugares criados
        if (places.isNotEmpty()) {
            Text("Meus Sítios", style = MaterialTheme.typography.titleMedium)
            places.forEach { place ->
                Text("- $place", style = MaterialTheme.typography.bodySmall)            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogoutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Terminar Sessão")
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}