package com.cmu.sweet.helpers

import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocationRepository {
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    fun updateLocation(location: Location) {
        val newLocation = Location(location)
        _location.update {
            newLocation
        }
        location.set(_location.value!!)
        Log.d("LocationRepository", "Updated location: $location")
    }
}

object LocationRepositoryProvider {
    val repository: LocationRepository by lazy { LocationRepository() }
}