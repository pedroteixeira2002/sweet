package com.cmu.sweet.helpers

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long) : Flow<Location>
    fun removeLocationUpdates()

    class LocationException(message:String): Exception()
}