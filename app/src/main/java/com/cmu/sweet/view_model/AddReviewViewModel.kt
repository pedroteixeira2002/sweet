package com.cmu.sweet.view_model

import android.app.Application
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.ReviewRepository
import com.cmu.sweet.ui.state.AddReviewUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class AddReviewViewModel(
    application: Application,
    private val establishmentRepo: EstablishmentRepository,
    private val reviewRepo: ReviewRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState

    private val _establishment = MutableLiveData<Establishment>()
    val establishment: LiveData<Establishment> = _establishment

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun loadEstablishment(establishmentId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = establishmentRepo.fetchById(establishmentId)
            _loading.value = false
            if (result.isSuccess) {
                _establishment.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }


    fun addReview(
        rating: Int,
        comment: String,
        photos: List<Uri>,
        priceRating: Int
    ) {

        val state = _uiState.value
        val est = _establishment.value ?: run {
            _error.value = "Establishment not loaded"
            return
        }

        if (rating <= 0 || comment.isBlank()) {
            _uiState.value = state.copy(
                isSubmitting = false,
                errorMessage = "Preencha todos os campos obrigatórios."
            )
            return
        }

        val addedBy = FirebaseAuth.getInstance().currentUser?.uid
        if (addedBy == null) {
            _uiState.value =
                state.copy(isSubmitting = false, errorMessage = "Usuário não autenticado.")
            return
        }


        viewModelScope.launch {
            _loading.value = true
            val storageRootRef = FirebaseStorage.getInstance(
                "gs://bionic-slate-470122-c5.firebasestorage.app"
            ).reference

            val id = UUID.randomUUID().toString()
            photos.forEachIndexed { index, uri ->
                uploadPhotoRobust(getApplication(), storageRootRef, uri, id, index + 1)
            }

            val review = Review(
                id = id,
                establishmentId = est.id,
                userId = addedBy,
                rating = rating,
                priceRating = priceRating,
                comment = comment,
            )

            val result = reviewRepo.add(review)
            _loading.value = false

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                _uiState.value = state.copy(isSubmitting = false, errorMessage = null)
            }
        }
    }

    private suspend fun uploadPhotoRobust(
        app: Application,
        storageRootRef: StorageReference,
        uri: Uri,
        reviewId: String,
        count: Int
    ): String? {
        val contentResolver = app.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

        val ref = storageRootRef.child("reviews/$reviewId/$count.$ext")

        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Timber.w(e, "Upload failed")
            null
        }
    }

    class Factory(
        private val application: Application,
        private val establishmentRepo: EstablishmentRepository,
        private val reviewRepo: ReviewRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddReviewViewModel::class.java)) {
                return AddReviewViewModel(application, establishmentRepo, reviewRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
