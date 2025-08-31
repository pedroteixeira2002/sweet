package com.cmu.sweet.view_model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmu.sweet.data.local.repository.EstablishmentRepository
import com.cmu.sweet.ui.components.fetchAddressSuggestions
import com.cmu.sweet.ui.state.AddEstablishmentUiState
import com.cmu.sweet.utils.getLatLngFromAddress
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddEstablishmentViewModel(
    application: Application,
    private val repository: EstablishmentRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddEstablishmentUiState())
    val uiState: StateFlow<AddEstablishmentUiState> = _uiState

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName, errorMessage = null)
    }

    fun onAddressChange(newAddress: String) {
        _uiState.value = _uiState.value.copy(address = newAddress, errorMessage = null)
    }

    fun onTypeChange(newType: String) {
        _uiState.value = _uiState.value.copy(type = newType, errorMessage = null)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription, errorMessage = null)
    }

    fun addEstablishment(context: Context) {
        val state = _uiState.value
        if (state.name.isBlank() || state.address.isBlank() || state.type.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Preencha todos os campos obrigatórios.")
            return
        }
        val addedBy = FirebaseAuth.getInstance().currentUser?.uid
        if (addedBy == null) {
            _uiState.value = state.copy(errorMessage = "Usuário não autenticado.")
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val (lat, lng) = getLatLngFromAddress(context, state.address)
                val result = repository.addEstablishment(
                    state.name, state.address, state.description, state.type, lat, lng, addedBy
                )
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = "Falha ao adicionar: ${e.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Falha ao obter coordenadas: ${e.message}"
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    class Factory(
        private val application: Application,
        private val repository: EstablishmentRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddEstablishmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddEstablishmentViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}