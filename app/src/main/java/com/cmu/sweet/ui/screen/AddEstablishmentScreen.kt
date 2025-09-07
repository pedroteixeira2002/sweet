package com.cmu.sweet.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.view_model.AddEstablishmentViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEstablishmentScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val db = SweetDatabase.getInstance(application)
    val dao = db.establishmentDao()
    val firestore = FirebaseFirestore.getInstance()

    val repository = EstablishmentRepository(firestore,dao)

    val viewModel: AddEstablishmentViewModel = viewModel(
        factory = AddEstablishmentViewModel.Factory(application, repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var addressSuggestions by remember { mutableStateOf(listOf<String>()) }
    var addressDropdownExpanded by remember { mutableStateOf(false) }
    val addressInput = uiState.address

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar(
                message = "Estabelecimento adicionado com sucesso!",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage!!,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Novo Estabelecimento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null && uiState.name.isBlank()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null && uiState.name.isBlank()
            )

            Box {
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { viewModel.onAddressChange(it) },
                    label = { Text("Morada") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage != null && uiState.address.isBlank()
                )
                DropdownMenu(
                    expanded = addressDropdownExpanded,
                    onDismissRequest = { addressDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    addressSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.onAddressChange(suggestion)
                                addressDropdownExpanded = false
                                addressSuggestions = emptyList()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.type,
                onValueChange = { viewModel.onTypeChange(it) },
                label = { Text("Tipo (ex: Café, Restaurante)") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null && uiState.type.isBlank()
            )

            if (uiState.errorMessage != null && !uiState.isSubmitting && !uiState.success) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.addEstablishment(context) },
                enabled = !uiState.isSubmitting &&
                        uiState.name.isNotBlank() &&
                        uiState.address.isNotBlank() &&
                        uiState.type.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("A adicionar...")
                } else {
                    Text("Adicionar")
                }
            }
        }
    }

}

