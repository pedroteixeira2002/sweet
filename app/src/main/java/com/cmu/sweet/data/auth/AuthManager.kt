package com.cmu.sweet.data.auth

import com.cmu.sweet.data.local.entities.UserEntity
import com.cmu.sweet.data.local.repository.ProfileStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthManager(
    private val firebaseAuth: FirebaseAuth,
    private val profileStore: ProfileStore // For creating profile doc after registration
) {

    /**
     * Registers a new user with Firebase Authentication and then creates their profile document in Firestore.
     */
    suspend fun registerUserWithProfile(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            // 1. Create Firebase Auth user
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Timber.e("Firebase user is null after auth creation for email: $email")
                return Result.failure(Exception("Registration failed: User not found after creation."))
            }
            Timber.i("Firebase Auth user created: ${firebaseUser.uid} for email: $email")

            // 2. Create profile document in Firestore via ProfileStore
            val newUserProfile = UserEntity(id = firebaseUser.uid, name = name, email = email)
            val profileResult = profileStore.createProfile(newUserProfile)

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
     * Logs in an existing user with email and password.
     */
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Timber.w("Login successful but FirebaseUser is null for email: $email")
                return Result.failure(Exception("Login failed: User data not available after sign-in."))
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Timber.e(e, "Error during login for email: $email")
            Result.failure(e)
        }
    }

    /**
     * Logs out the currently authenticated user.
     */
    fun logoutUser() {
        try {
            firebaseAuth.signOut()
            Timber.i("User logged out.")
        } catch (e: Exception) {
            Timber.e(e, "Error during sign out.")
            // Sign out itself doesn't typically throw, but good to be defensive.
        }
    }

    /**
     * Gets the currently authenticated FirebaseUser, or null if no user is logged in.
     */
    fun getCurrentFirebaseAuthUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Provides a Flow that emits the current FirebaseUser whenever the auth state changes.
     */
    fun getFirebaseAuthFlow(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            Timber.d("Closing FirebaseAuth listener.")
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * Sends a password reset email to the given email address.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.i("Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error sending password reset email to $email")
            Result.failure(e)
        }
    }

    /**
     * Updates the email for the currently authenticated user in Firebase Authentication.
     * Note: This is a sensitive operation and might require recent re-authentication.
     * The email in the Firestore profile document must be updated separately via ProfileStore.
     */
    suspend fun updateUserEmailInAuth(newEmail: String): Result<Unit> {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            try {
                currentUser.updateEmail(newEmail).await()
                Timber.i("Firebase Auth email updated to $newEmail for user ${currentUser.uid}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error updating Firebase Auth email for user ${currentUser.uid}")
                Result.failure(e) // Handle specific exceptions like "FirebaseAuthRecentLoginRequiredException" in ViewModel
            }
        } else {
            Timber.w("Cannot update email: No user currently authenticated.")
            Result.failure(IllegalStateException("No user authenticated to update email."))
        }
    }

    /**
     * Updates the password for the currently authenticated user.
     * Note: This is a sensitive operation and might require recent re-authentication.
     */
    suspend fun updateUserPasswordInAuth(newPassword: String): Result<Unit> {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            try {
                currentUser.updatePassword(newPassword).await()
                Timber.i("Firebase Auth password updated for user ${currentUser.uid}")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error updating Firebase Auth password for user ${currentUser.uid}")
                Result.failure(e) // Handle specific exceptions
            }
        } else {
            Timber.w("Cannot update password: No user currently authenticated.")
            Result.failure(IllegalStateException("No user authenticated to update password."))
        }
    }
}
