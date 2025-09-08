package com.cmu.sweet.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.state.EditProfileUiState
import com.cmu.sweet.view_model.EditProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
    ) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userDao = SweetDatabase.getInstance(application).userDao()
    val userRepository = UserRepository(firestore, userDao, auth)
    val viewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModel.Factory(application, userRepository)
    )
    val state by viewModel.uiState.observeAsState(EditProfileUiState())

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editar Perfil") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveProfile() },
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.nameError != null
                )
                state.nameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.bioInput,
                    onValueChange = { viewModel.updateBio(it) },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.emailDisplay,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isSaving) {
                    CircularProgressIndicator()
                }

                state.generalError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
