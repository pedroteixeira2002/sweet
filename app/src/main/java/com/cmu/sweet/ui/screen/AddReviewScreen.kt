package com.cmu.sweet.ui.screen

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import com.cmu.sweet.view_model.AddReviewViewModel
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    navController: NavController,
    establishmentId: String?,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val db = SweetDatabase.getInstance(application)
    val establishmentDao = db.establishmentDao()
    val reviewDao = db.reviewDao()

    val firestore = FirebaseFirestore.getInstance()

    val establishmentRepo = EstablishmentRepository(firestore, establishmentDao)
    val reviewRepo = ReviewRepository(firestore, reviewDao)
    val viewModel: AddReviewViewModel = viewModel(
        factory = AddReviewViewModel.Factory(application, establishmentRepo, reviewRepo)
    )

    establishmentId?.let {
        LaunchedEffect(it) {
            viewModel.loadEstablishment(it)
        }
    }


    val establishment by viewModel.establishment.observeAsState()
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()


    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var priceRating by remember { mutableIntStateOf(0) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(), // safer than GetMultipleContents
    ) { uris ->
        if (uris.isNotEmpty()) {
            val grantedUris = mutableListOf<Uri>()
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    grantedUris.add(uri)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to take persistable URI permission")
                }
            }
            photoUris = (photoUris + grantedUris).take(5) // max 5 photos
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Review") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            establishment?.let { est ->
                Text("Reviewing: ${est.name}", style = MaterialTheme.typography.headlineSmall)
                Text(est.address, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        navController.navigate("establishment/${est.id}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Establishment Page")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⭐ Star Rating
            Text("Rating")
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    val starValue = index + 1
                    Icon(
                        imageVector = if (starValue <= rating) Icons.Filled.Star else Icons.Filled.Star, // same icon but different tint
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { rating = starValue },
                        tint = if (starValue <= rating) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // 💵 Price Rating
            Text("Price Rating")
            Row {
                (1..4).forEach { i ->
                    Text(
                        text = "$".repeat(i),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (priceRating == i) Color.Gray.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .clickable { priceRating = i }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 📝 Text Review
            Text("Comment")
            TextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                placeholder = { Text("Write your review...") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 📷 Photo Upload
            Text("Upload Photos (max 5)")
            Button(onClick = { photoPickerLauncher.launch(arrayOf("image/*")) }) {
                Text("Select Photos")
            }
            LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                items(photoUris.size) { index ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUris[index]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error
            error?.let { Text(it, color = Color.Red) }

            // Submit Button
            Button(
                onClick = {
                    viewModel.addReview(
                        rating,
                        comment,
                        photoUris,
                        priceRating
                    )
                    navController.popBackStack()
                },
                enabled = !loading && rating > 0 && comment.isNotBlank() && establishment != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Submitting..." else "Submit Review")
            }
        }
    }
}
