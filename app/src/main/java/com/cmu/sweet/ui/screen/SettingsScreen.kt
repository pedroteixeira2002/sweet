package com.cmu.sweet.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cmu.sweet.R
import com.cmu.sweet.ui.components.LanguageDropdown
import com.cmu.sweet.view_model.LightSensorViewModel
import com.cmu.sweet.view_model.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    lightSensorViewModel: LightSensorViewModel
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()




    Scaffold(
    ) { innerPadding ->
        SettingsScreenOptions(
            paddingValues = innerPadding,
            isDarkMode = isDarkMode,
            selectedLanguage = selectedLanguage,
            onDarkModeChange = { settingsViewModel.updateDarkMode(it) },
            onLanguageChange = { settingsViewModel.updateSelectedLanguage(it) },
        )
    }

}

@Composable
fun SettingsScreenOptions(
    paddingValues: PaddingValues,
    isDarkMode: Boolean,
    selectedLanguage: String,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = remember { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(if (isLandscape) 24.dp else 16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(id = R.string.settings),
            fontSize = if (isLandscape) 28.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(if (isLandscape) Alignment.Start else Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(if (isLandscape) 32.dp else 16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RowSetting(label = stringResource(id = R.string.dark_mode)) {
                Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
            }
            Spacer(modifier = Modifier.height(16.dp))

            LanguageDropdown(
                label = stringResource(id = R.string.language),
                currentValue = selectedLanguage,
                options = listOf(
                    stringResource(id = R.string.english),
                    stringResource(id = R.string.portuguese)
                ),
                onOptionSelected = onLanguageChange
            )

        }
    }
}


@Composable
fun RowSetting(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp), // Add some vertical padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        content()
    }
}