package com.cmu.sweet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    suggestions: List<AutocompletePrediction>,
    onSuggestionClick: (AutocompletePrediction) -> Unit
) {
    val textFieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, textFieldShape)
                .border(1.dp, Color.Gray, textFieldShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Pesquisar morada") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

        }

        Spacer(modifier = Modifier.height(4.dp))

        if (suggestions.isNotEmpty()) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { /* ignore */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, textFieldShape)
            ) {
                suggestions.forEach { prediction ->
                    DropdownMenuItem(
                        text = { Text(prediction.getFullText(null).toString()) },
                        onClick = { onSuggestionClick(prediction) }
                    )
                }
            }
        }
    }
}
