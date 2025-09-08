package com.cmu.sweet.data.repository

import androidx.lifecycle.LiveData
import com.cmu.sweet.data.local.dao.ReviewDao
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.mappers.toDto
import com.cmu.sweet.data.mappers.toLocal
import com.cmu.sweet.data.remote.dto.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ReviewRepository(
    private val firestore: FirebaseFirestore,
    private val dao: ReviewDao
) {
    companion object {
        private const val COLLECTION = "reviews"
    }

    /** Local LiveData */
    fun getAll(): LiveData<List<Review>> = dao.getAll()
    fun getById(id: String): LiveData<Review?> = dao.getById(id)
    fun getByUser(userId: String): LiveData<List<Review>> = dao.getByUserId(userId)

    /** Sync all from Firestore to Room */
    suspend fun syncAll(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION).get().await()
            val dtos = snapshot.toObjects(ReviewDto::class.java)
            dao.insertAll(dtos.map { it.toLocal() })
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing reviews")
            Result.failure(e)
        }
    }

    suspend fun getByEstablishment(estId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("establishmentId", estId)
                .get()
                .await()
            val dtos = snapshot.toObjects(ReviewDto::class.java)
            val entities = dtos.map { it.toLocal() }
            Result.success(entities)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching reviews for establishment $estId")
            Result.failure(e)
        }
    }

    suspend fun getByUserOnce(userId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val dtos = snapshot.toObjects(ReviewDto::class.java)
            val entities = dtos.map { it.toLocal() }
            Result.success(entities)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching reviews for user $userId")
            Result.failure(e)
        }
    }

    suspend fun add(review: Review): Result<Unit> {
        return try {
            val dto = review.toDto() // keep the same ID
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding review")
            Result.failure(e)
        }
    }

    /** Update existing review */
    suspend fun update(review: Review): Result<Unit> {
        return try {
            val dto = review.toDto()
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating review")
            Result.failure(e)
        }
    }

    /** Delete review */
    suspend fun delete(id: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION).document(id).delete().await()
            dao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting review")
            Result.failure(e)
        }
    }

}
