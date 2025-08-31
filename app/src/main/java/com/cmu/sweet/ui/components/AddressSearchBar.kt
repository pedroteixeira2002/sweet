package com.cmu.sweet.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddressSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    radiusMeters: Float,
    onRadiusChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        // Campo de pesquisa
        TextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            label = { Text("Pesquisar morada") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown de sugestões
        DropdownMenu(
            expanded = suggestions.isNotEmpty(),
            onDismissRequest = { /* ignorar */ }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Slider para distância
        Column {
            Text(
                text = "Distância: ${
                    if (radiusMeters < 1000) "${radiusMeters.toInt()} m"
                    else String.format("%.1f km", radiusMeters / 1000f)
                }"
            )
            Slider(
                value = radiusMeters,
                onValueChange = onRadiusChange,
                valueRange = 250f..20000f,
                steps = 19
            )
        }
    }
}
