package com.cmu.sweet.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cmu.sweet.helpers.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    init {
        // Collect initial values from DataStore
        viewModelScope.launch {
            settingsDataStore.isDarkModeFlow.collect {
                _isDarkMode.value = it
            }
        }

        viewModelScope.launch {
            settingsDataStore.selectedLanguageFlow.collect {
                _selectedLanguage.value = it
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateDarkMode(enabled)
        }
    }

    fun updateSelectedLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.updateLanguage(language)
        }
    }
}