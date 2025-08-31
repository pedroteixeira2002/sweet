package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.ui.state.EditProfileUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class EditProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableLiveData(EditProfileUiState())
    val uiState: LiveData<EditProfileUiState> = _uiState

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            try {
                val snapshot = firestore.collection("users").document(user.uid).get().await()
                val name = snapshot.getString("displayName") ?: ""
                val email = user.email ?: ""
                val bio = snapshot.getString("bio") ?: ""

                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    userId = user.uid,
                    nameInput = name,
                    emailDisplay = email,
                    bioInput = bio,
                    initialName = name,
                    initialBio = bio
                )
            } catch (e: Exception) {
                Timber.Forest.e(e, "Erro ao carregar dados do utilizador")
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    generalError = "Erro ao carregar dados: ${e.message}"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value?.copy(
            nameInput = name,
            hasChanges = true
        )
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value?.copy(
            bioInput = bio,
            hasChanges = true
        )
    }


    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value ?: return@launch
            if (state.userId.isEmpty()) return@launch

            _uiState.value = state.copy(isSaving = true, generalError = null)


            try {
                val updates = mutableMapOf<String, Any>(
                    "displayName" to state.nameInput,
                    "bio" to state.bioInput
                )

                firestore.collection("users").document(state.userId).update(updates).await()

                _uiState.value = state.copy(
                    isSaving = false,
                    hasChanges = false,
                    navigateBack = true
                )

            } catch (e: Exception) {
                Timber.Forest.e(e, "Falha update Firestore")
                _uiState.value = state.copy(
                    isSaving = false,
                    generalError = "Falha ao guardar alterações: ${e.message}"
                )
            }
        }
    }
}