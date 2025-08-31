package com.cmu.sweet.data.local.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ProfileStore(private val firestore: FirebaseFirestore) {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Creates a new user profile document in Firestore.
     * Typically called after successful Firebase Authentication.
     */
    suspend fun createProfile(user: User): Result<Unit> {
        return try {
            if (user.id.isBlank()) {
                Timber.Forest.e("User ID is blank, cannot create profile.")
                return Result.failure(IllegalArgumentException("User ID is required to create a profile."))
            }
            firestore.collection(USERS_COLLECTION).document(user.id).set(user).await()
            Timber.Forest.i("Profile created for user ID: ${user.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error creating Firestore profile for user ID: ${user.id}")
            Result.failure(e)
        }
    }

    /**
     * Fetches a user's profile from Firestore once.
     */
    suspend fun getProfile(userId: String): Result<User?> {
        return try {
            if (userId.isBlank()) {
                Timber.Forest.e("User ID is blank, cannot get profile.")
                return Result.failure(IllegalArgumentException("User ID is required to get a profile."))
            }
            val documentSnapshot = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)
                Result.success(user)
            } else {
                Timber.Forest.w("No profile found for user ID: $userId")
                Result.success(null) // No profile found is a valid outcome
            }
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error fetching profile for user ID: $userId")
            Result.failure(e)
        }
    }

    /**
     * Observes real-time changes to a user's profile in Firestore.
     */
    fun observeProfile(userId: String): Flow<Result<User?>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(Result.failure(IllegalArgumentException("User ID cannot be empty for observation.")))
            close()
            return@callbackFlow
        }

        val userDocumentRef = firestore.collection(USERS_COLLECTION).document(userId)
        val listenerRegistration = userDocumentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.Forest.e(error, "Error listening to profile for ID: $userId")
                trySend(Result.failure(error))
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject<User>()?.copy(id = snapshot.id)
                trySend(Result.success(user))
            } else {
                Timber.Forest.w("Profile document not found or is null during observation for ID: $userId")
                trySend(Result.success(null)) // User document doesn't exist
            }
        }
        awaitClose {
            Timber.Forest.d("Closing Firestore listener for user: $userId")
            listenerRegistration.remove()
        }
    }


    /**
     * Updates an existing user's profile in Firestore.
     * Uses SetOptions.merge() to only update provided fields if the User object has nulls.
     * Alternatively, build a map of non-null fields for more precise updates.
     */
    suspend fun updateProfile(userUpdates: User): Result<Unit> {
        return try {
            if (userUpdates.id.isBlank()) {
                Timber.Forest.e("User ID is blank, cannot update profile.")
                return Result.failure(IllegalArgumentException("User ID is required to update a profile."))
            }
            // Using SetOptions.merge to only update fields present in userUpdates
            // If userUpdates has null for a field that exists in Firestore, merge won't delete it.
            // If you want to explicitly set a field to null (delete it), you'd use FieldValue.delete() in a map.
            firestore.collection(USERS_COLLECTION).document(userUpdates.id)
                .set(userUpdates, SetOptions.merge()).await()
            Timber.Forest.i("Profile updated for user ID: ${userUpdates.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error updating profile for user ID: ${userUpdates.id}")
            Result.failure(e)
        }
    }
}