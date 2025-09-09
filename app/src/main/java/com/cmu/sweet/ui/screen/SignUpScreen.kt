package com.cmu.sweet.ui.screen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmu.sweet.R
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.view_model.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val userDao = SweetDatabase.getInstance(context).userDao()
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            context.applicationContext as Application,
            userDao,
            firestore,
            firebaseAuth
        )
    )

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            onRegisterSuccess()
            viewModel.onRegistrationCompleteNotified()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.generalRegistrationError) {
        // Capture the error message in a local variable
        val errorMessage = state.generalRegistrationError
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage, // Use the local variable
                duration = SnackbarDuration.Short
            )
            viewModel.clearGeneralError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cake_48px),
                contentDescription = "SweetMe Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(26.dp))

            Text("Create your SweetMe account", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.nameInput,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.nameError != null,
                supportingText = {
                    state.nameError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.emailInput,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = state.emailError != null,
                supportingText = {
                    state.emailError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.passwordInput,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = state.passwordError != null,
                supportingText = {
                    state.passwordError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.confirmPasswordInput,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = state.confirmPasswordError != null,
                supportingText = {
                    state.confirmPasswordError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            // Capture the error message in a local variable for the Text display as well
            val currentGeneralError = state.generalRegistrationError
            if (currentGeneralError != null && snackbarHostState.currentSnackbarData == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currentGeneralError,
                    color = MaterialTheme.colorScheme.error
                ) // Use the local variable
            }


            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.attemptSignUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLoginClick, enabled = !state.isLoading) {
                Text("Already have an account? Login")
            }
        }
    }
}
