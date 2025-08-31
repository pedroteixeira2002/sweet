package com.cmu.sweet.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cmu.sweet.ui.navigation.BottomNavItem


/**
 * Um Composable reutilizável para a barra de navegação inferior principal do aplicativo.
 *
 * @param items A lista de [BottomNavItem] a serem exibidos.
 * @param currentSelectedItem O [BottomNavItem] atualmente selecionado.
 * @param onItemSelected Callback que é invocado quando um item é selecionado.
 * @param modifier O [Modifier] a ser aplicado a este layout.
 */
@Composable
fun AppBottomNavigationBar(
    items: List<BottomNavItem>,
    currentSelectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface, // Ou surfaceVariant, ou outra cor do seu tema
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentSelectedItem.route == item.route,
                onClick = { onItemSelected(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true, // Você pode definir como false se quiser mostrar o rótulo apenas para o item selecionado
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // Ou um tom de 'secondaryContainer'
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    // Para o preview, precisamos fornecer dados mockados
    val previewItems = listOf(BottomNavItem.Home, BottomNavItem.Leaderboard, BottomNavItem.Profile)
    MaterialTheme { // Use seu SweetTheme se ele já estiver configurado
        AppBottomNavigationBar(
            items = previewItems,
            currentSelectedItem = BottomNavItem.Home,
            onItemSelected = { /* Não faz nada no preview */ }
        )
    }
}

@Preview(showBackground = true, name = "Selected Leaderboard")
@Composable
fun AppBottomNavigationBarLeaderboardSelectedPreview() {
    val previewItems = listOf(BottomNavItem.Home, BottomNavItem.Leaderboard, BottomNavItem.Profile)
    MaterialTheme {
        AppBottomNavigationBar(
            items = previewItems,
            currentSelectedItem = BottomNavItem.Leaderboard,
            onItemSelected = { }
        )
    }
}

