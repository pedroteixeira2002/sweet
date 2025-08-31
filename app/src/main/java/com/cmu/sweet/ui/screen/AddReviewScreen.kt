package com.cmu.sweet.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // Para o Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    navController: NavController,
    establishmentId: String? // Pode ser nulo
) {
    // Você pode querer um ViewModel aqui para carregar detalhes do estabelecimento se o ID for fornecido,
    // ou para lidar com a lógica de submissão da review.

    LaunchedEffect(establishmentId) {
        if (establishmentId != null) {
            // Lógica para carregar detalhes do estabelecimento usando o ID
            println("AddReviewScreen: Carregar detalhes para o estabelecimento ID: $establishmentId")
        } else {
            // Lógica para quando nenhum ID de estabelecimento é fornecido
            // (ex: mostrar um seletor de estabelecimento ou um campo de busca)
            println("AddReviewScreen: Nenhum ID de estabelecimento fornecido. Usuário precisará selecionar um.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (establishmentId != null) "Adicionar Review" else "Selecionar Estabelecimento e Adicionar Review") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tela de Adicionar Review",
                style = MaterialTheme.typography.headlineSmall
            )
            if (establishmentId != null) {
                Text("Review para o Estabelecimento ID: $establishmentId")
            } else {
                Text("Por favor, selecione um estabelecimento para avaliar.")
                // Aqui você pode adicionar um campo de busca ou um seletor de estabelecimento
            }
            // Aqui você adicionará campos para a avaliação (estrelas, texto do comentário, etc.)
            // e um botão para submeter.
        }
    }
}

@Preview(showBackground = true, name = "Add Review With ID")
@Composable
fun AddReviewScreenWithIdPreview() {
    MaterialTheme {
        AddReviewScreen(navController = rememberNavController(), establishmentId = "test-123")
    }
}

@Preview(showBackground = true, name = "Add Review Without ID")
@Composable
fun AddReviewScreenWithoutIdPreview() {
    MaterialTheme {
        AddReviewScreen(navController = rememberNavController(), establishmentId = null)
    }
}
