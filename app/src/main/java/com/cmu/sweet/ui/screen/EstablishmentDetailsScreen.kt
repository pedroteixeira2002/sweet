package com.cmu.sweet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Para ícones de placeholder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Para carregar imagens da URL
import coil.request.ImageRequest
import com.cmu.sweet.R // Se tiver drawables de placeholder
import com.cmu.sweet.ui.state.EstablishmentDetails
import com.cmu.sweet.ui.state.ReviewUiModel
import com.cmu.sweet.view_model.EstablishmentDetailsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: EstablishmentDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.establishment?.name ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    uiState.establishment?.let {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.error != null) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryLoadDetails() }) {
                                Text("Tentar Novamente")
                            }
                        }
                    }
                }
            } else if (uiState.establishment != null) {
                val establishment = uiState.establishment!!

                // Seção de Fotos
                item {
                    PhotosSection(photos = establishment.photos, establishmentName = establishment.name)
                }

                // Seção de Informações Básicas
                item {
                    InfoSection(establishment = establishment)
                }

                // Seção de Descrição (se houver)
                establishment.description?.let { description ->
                    item {
                        DescriptionSection(description = description)
                    }
                }

                // Seção de Horário
                item {
                    OpeningHoursSection(openingHours = establishment.openingHours)
                }

                // Seção de Mini-Mapa
                item {
                    MiniMapSection(location = establishment.location, name = establishment.name)
                }

                // Seção de Avaliações
                item {
                    ReviewsSection(reviews = establishment.reviews)
                }

                // Espaço no final
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun PhotosSection(photos: List<String>, establishmentName: String) {
    if (photos.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Altura fixa para o carrossel de fotos
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(photos, key = { it }) { photoUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_launcher_background), // Substitua por um placeholder seu
                    error = painterResource(R.drawable.ic_launcher_background), // Substitua por um placeholder de erro
                    contentDescription = "Foto de $establishmentName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillParentMaxHeight() // Preenche a altura do LazyRow
                        .aspectRatio(4f / 3f) // Proporção da imagem
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ImageNotSupported, "Sem fotos disponíveis", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoSection(establishment: EstablishmentDetails) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(establishment.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107)) // Cor de estrela
            Spacer(Modifier.width(4.dp))
            Text("${establishment.rating}", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(8.dp))

        InfoRow(icon = Icons.Filled.LocationOn, text = establishment.address)
        establishment.phoneNumber?.let {
            Spacer(Modifier.height(4.dp))
            InfoRow(icon = Icons.Filled.Phone, text = it, isClickable = true, onClick = { /* Abrir discador */ })
        }
    }
}

@Composable
fun DescriptionSection(description: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Sobre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OpeningHoursSection(openingHours: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Horário", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        openingHours.forEach { hour ->
            Text("• $hour", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MiniMapSection(location: LatLng, name: String) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f) // Zoom mais próximo para detalhes
    }
    val markerState = rememberMarkerState(position = location)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Localização", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false, // Desabilitar scroll para um mini-mapa
                zoomGesturesEnabled = false
            )
        ) {
            Marker(state = markerState, title = name)
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<ReviewUiModel>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Avaliações (${reviews.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (reviews.isEmpty()) {
            Text(
                "Ainda não há avaliações.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewUiModel) {
    Card(
        modifier = Modifier.width(300.dp), // Largura fixa para os cards de review no carrossel
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.userAvatarUrl ?: R.drawable.ic_launcher_background, // Placeholder para avatar
                    contentDescription = "Avatar de ${review.userName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(review.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(review.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.rating.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String, isClickable: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if(isClickable && onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

