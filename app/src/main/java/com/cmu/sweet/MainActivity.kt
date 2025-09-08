package com.cmu.sweet

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.remote.dto.EstablishmentDto
import com.cmu.sweet.helpers.LanguageContextWrapper
import com.cmu.sweet.helpers.LocationRepositoryProvider
import com.cmu.sweet.helpers.LocationService
import com.cmu.sweet.ui.navigation.AppNavGraph
import com.cmu.sweet.ui.theme.SweetTheme
import com.cmu.sweet.utils.addGeofence
import com.cmu.sweet.utils.distanceInMeters
import com.cmu.sweet.view_model.SettingsViewModel
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val locationRepository = LocationRepositoryProvider.repository
    private val firestore = FirebaseFirestore.getInstance()

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAj5euiceTomJVILEJkuTTLgO43LnaDcCc")
        }
        setContent {

            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode = settingsViewModel.isDarkMode.collectAsState()
            val selectedLanguage = settingsViewModel.selectedLanguage.collectAsState()
            LanguageContextWrapper.wrap(this, selectedLanguage.value)
            SweetTheme(isDarkMode.value) {
                    AppNavGraph(
                        settingsViewModel)
                }
            }
        requestPermissions()
    }

    @SuppressLint("InlinedApi")
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                0
            )
        } else {
            setupEverything()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    private fun setupEverything() {
        setupNotificationChannels()
        startLocationService()

        lifecycleScope.launch {
            LocationRepositoryProvider.repository.location.collect { loc ->
                if (loc != null) {
                    Timber.d("User location recebida: ${loc.latitude}, ${loc.longitude}")
                    addGeofencesFromFirestore() // agora adiciona geofences baseadas na localização real
                    cancel() // só precisamos da primeira localização
                }
            }
        }
    }

    private fun setupNotificationChannels() {
        val channel = NotificationChannel(
            "location",
            "Location Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notifications for location-based events" }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        Timber.tag("App").d("Notification channel created")
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        LocationService.setLocationRepository(locationRepository)
        startService(serviceIntent)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    private suspend fun addGeofencesFromFirestore() {
        try {
            val snapshot = firestore.collection("establishments").get().await()
            val dtos = snapshot.toObjects(EstablishmentDto::class.java)
            val establishments = dtos.map { dto ->
                Establishment(
                    id = dto.id,
                    name = dto.name,
                    address = dto.address,
                    type = dto.type,
                    description = dto.description,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    addedBy = dto.addedBy
                )
            }

            val userLocation = locationRepository.location.value

            val geofencesToAdd = if (userLocation != null) {
                // Se temos localização, filtra os 100 mais próximos
                establishments
                    .map { est ->
                        val dist = distanceInMeters(
                            userLocation.latitude, userLocation.longitude,
                            est.latitude, est.longitude
                        )
                        Pair(est, dist)
                    }
                    .sortedBy { it.second }
                    .take(100)
                    .map { it.first }
            } else {
                Timber.tag("Geofence")
                    .w("User location unknown. Adding first 100 establishments by default.")
                establishments.take(100)
            }

            geofencesToAdd.forEach { est ->
                addGeofence(this@MainActivity, est.latitude, est.longitude, 50f)
                Timber.tag("Geofence")
                    .d("Added geofence for ${est.name} at (${est.latitude}, ${est.longitude})")
            }

            Timber.tag("Geofence").d("${geofencesToAdd.size} geofences added")

        } catch (e: Exception) {
            Timber.tag("Geofence").e(e, "Failed to fetch establishments or add geofences")
        }
    }

}
