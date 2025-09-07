package com.cmu.sweet.data.repository

import androidx.lifecycle.LiveData
import com.cmu.sweet.data.local.dao.UserDao
import com.cmu.sweet.data.local.entities.User
import com.cmu.sweet.data.mappers.toDto
import com.cmu.sweet.data.mappers.toLocal
import com.cmu.sweet.data.remote.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val dao: UserDao,
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val COLLECTION = "users"
    }

    /** Local LiveData */
    fun getAll(): LiveData<List<User>> = dao.getAll()
    fun getById(id: String): LiveData<User?> = dao.getById(id)

    /** Sync all users from Firestore to Room */
    suspend fun syncAll(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION).get().await()
            val dtos = snapshot.toObjects(UserDto::class.java)
            dao.insertAll(dtos.map { it.toLocal() })
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing users")
            Result.failure(e)
        }
    }

    /** Add a new user */
    suspend fun add(user: User): Result<Unit> {
        return try {
            val dto = user.toDto().copy(id = firestore.collection(COLLECTION).document().id)
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding user")
            Result.failure(e)
        }
    }

    /** Update existing user */
    suspend fun update(user: User): Result<Unit> {
        return try {
            val dto = user.toDto()
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating user")
            Result.failure(e)
        }
    }

    /** Get currently authenticated Firebase user */
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    suspend fun getProfile(userId: String): Result<User?> {
        return try {
            val localUser = dao.getByIdSuspend(userId)
            if (localUser != null) return Result.success(localUser)

            val remoteResult = getProfileRemote(userId)
            remoteResult.onSuccess { user -> user?.let { dao.insert(it) } }
            remoteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch profile directly from Firestore only.
     */
    private suspend fun getProfileRemote(userId: String): Result<User?> {
        return try {
            val documentSnapshot = firestore.collection(COLLECTION).document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Delete user */
    suspend fun delete(id: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION).document(id).delete().await()
            dao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user")
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

}
