package com.cmu.sweet.ui.auth

import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Para Toasts ou Snackbar
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import para viewModel()
import coil.compose.rememberAsyncImagePainter
// Remova os imports diretos do UserRepository e FirebaseRepository se não forem mais usados aqui
// import com.cmu.sweet.data.remote.FirebaseRepository
// import com.cmu.sweet.data.repository.UserRepository

@Composable
fun SignUpScreen(
    // viewModel: SignUpViewModel = viewModel(), // Se você tiver uma factory padrão ou Hilt
    // Para instanciar manualmente sem Hilt (ou com factory customizada):
    viewModelFactory: () -> SignUpViewModel = { SignUpViewModel() },
    viewModel: SignUpViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return viewModelFactory() as T
        }
    }),
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var validationErrorMessage by remember { mutableStateOf("") } // Erro de validação local

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // Observar o estado do ViewModel
    val uiState = viewModel.uiState
    val context = LocalContext.current // Para exibir Snackbar ou Toast

    // Lidar com o sucesso do registro ou erro do ViewModel
    LaunchedEffect(uiState) {
        if (uiState.registrationSuccess) {
            // Mostrar mensagem de sucesso (opcional)
            // Toast.makeText(context, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
            viewModel.consumeRegistrationSuccess() // Resetar o estado
        }
        if (uiState.error != null) {
            // Mostrar mensagem de erro do ViewModel
            // Aqui você pode usar uma Snackbar ou atualizar validationErrorMessage
            validationErrorMessage = uiState.error!! // Sobrescreve o erro de validação local
            // viewModel.consumeError() // Consumir o erro após mostrar
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create your SweetMe account", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Image Picker (código existente)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Add Photo", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // OutlinedTextFields para name, email, password, confirmPassword (código existente)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; validationErrorMessage = "" }, // Limpar erro ao digitar
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = validationErrorMessage.isNotEmpty() && name.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; validationErrorMessage = "" },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = validationErrorMessage.isNotEmpty() && (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; validationErrorMessage = "" },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = validationErrorMessage.isNotEmpty() && (password.isBlank() || password.length < 6)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; validationErrorMessage = "" },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = validationErrorMessage.isNotEmpty() && (confirmPassword.isBlank() || password != confirmPassword)
        )

        // Exibir mensagem de erro (de validação local ou do ViewModel)
        if (validationErrorMessage.isNotEmpty()) {
            Text(validationErrorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            // Se o erro vier do ViewModel, você pode querer limpá-lo após ser exibido
            LaunchedEffect(validationErrorMessage) {
                if (uiState.error == validationErrorMessage) { // Se o erro atual for do ViewModel
                    viewModel.consumeError()
                }
            }
        }
        // Indicador de carregamento
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp)) // Ajustar espaçamento

        Button(
            onClick = {
                // Validations locais primeiro
                val currentValidationError = when {
                    name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                        "All fields are required"
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                        "Invalid email address"
                    password != confirmPassword -> "Passwords do not match"
                    password.length < 6 -> "Password must be at least 6 characters"
                    else -> ""
                }
                validationErrorMessage = currentValidationError

                if (currentValidationError.isEmpty()) {
                    // Se as validações locais passarem, chamar o ViewModel
                    viewModel.registerUser(
                        name = name.trim(),
                        email = email.trim(),
                        password = password, // A senha não deve ter trim()
                        profileImageUri = profileImageUri
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading // Desabilitar botão durante o carregamento
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onLoginClick, enabled = !uiState.isLoading) {
            Text("Already have an account? Login")
        }
    }
}

