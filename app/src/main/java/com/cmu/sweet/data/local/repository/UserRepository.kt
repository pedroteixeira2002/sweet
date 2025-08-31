package com.cmu.sweet.data.local.repository

import androidx.room.util.copy
import com.cmu.sweet.data.local.dao.UserDao
import com.cmu.sweet.data.local.entities.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
) {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Creates a new user profile document in Firestore.
     * Typically called after successful Firebase Authentication.
     */
    suspend fun createProfile(user: UserEntity): Result<Unit> {
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
     * Registers a new user with Firebase Authentication and then creates their profile document in Firestore.
     */
    suspend fun registerUserWithProfile(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Timber.e("Firebase user is null after auth creation for email: $email")
                return Result.failure(Exception("Registration failed: User not found after creation."))
            }
            Timber.i("Firebase Auth user created: ${firebaseUser.uid} for email: $email")

            // 2. Create profile document in Firestore via ProfileStore
            val newUserProfile = UserEntity(id = firebaseUser.uid, name = name, email = email)
            val profileResult = createProfile(newUserProfile)

            profileResult.fold(
                onSuccess = {
                    Timber.i("User ${firebaseUser.uid} registered and profile document created.")
                    Result.success(firebaseUser)
                },
                onFailure = { profileError ->
                    Timber.e(profileError, "Auth user ${firebaseUser.uid} created, but Firestore profile creation failed.")
                    // Consider the implications: User is in Auth but not fully set up in Firestore.
                    // For now, return a specific error. More complex handling might involve trying to delete the auth user.
                    Result.failure(Exception("Authentication successful, but profile data setup failed.", profileError))
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error during registration process for email: $email")
            Result.failure(e) // Catches Firebase Auth exceptions primarily
        }
    }

    /**
     * Fetches a user's profile from Firestore once.
     */
    suspend fun getProfile(userId: String): Result<UserEntity?> {
        return try {
            if (userId.isBlank()) {
                Timber.Forest.e("User ID is blank, cannot get profile.")
                return Result.failure(IllegalArgumentException("User ID is required to get a profile."))
            }
            val documentSnapshot = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject<UserEntity>()?.copy(id = documentSnapshot.id)
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
     * Gets the currently authenticated FirebaseUser, or null if no user is logged in.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }


    /**
     * Updates an existing user's profile in Firestore.
     * Uses SetOptions.merge() to only update provided fields if the User object has nulls.
     * Alternatively, build a map of non-null fields for more precise updates.
     */
    suspend fun updateProfile(userUpdates: UserEntity): Result<Unit> {
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