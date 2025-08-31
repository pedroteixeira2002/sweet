package com.cmu.sweet // <<< VERIFY THIS PACKAGE NAME

import android.app.Application
import com.cmu.sweet.data.local.repository.UserRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber


class SweetApplication : Application() {

    lateinit var placesClient: PlacesClient
        private set

    // Lazy initialization for Firebase services
    private val firestoreInstance: FirebaseFirestore by lazy {
        Timber.d("Initializing Firestore instance...")
        FirebaseFirestore.getInstance()
    }

    private val firebaseAuthInstance: FirebaseAuth by lazy {
        Timber.d("Initializing FirebaseAuth instance...")
        FirebaseAuth.getInstance()
    }

    // Lazy initialization for your managers/stores
    internal val userRepository: UserRepository by lazy {
        Timber.d("Initializing ProfileStore...")
        UserRepository(firestoreInstance)
    }

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAj5euiceTomJVILEJkuTTLgO43LnaDcCc")
        }
        placesClient = Places.createClient(this)
        Timber.i("SweetApplication onCreate completed.")
    }
}