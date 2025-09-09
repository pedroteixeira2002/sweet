package com.cmu.sweet.data.repository

import androidx.lifecycle.LiveData
import com.cmu.sweet.data.local.dao.ReviewDao
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.mappers.toDto
import com.cmu.sweet.data.mappers.toLocal
import com.cmu.sweet.data.remote.dto.ReviewDto
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import kotlin.compareTo
import kotlin.text.get

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

    suspend fun canUserReview(
        establishmentId: String,
        userLocation: LatLng,
        userId: String
    ): Boolean {
        return try {
            Timber.d("🔍 Checking if user $userId can review establishment $establishmentId")

            // Fetch previous reviews
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("establishmentId", establishmentId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val reviews = snapshot.toObjects(ReviewDto::class.java)
            Timber.d("📑 Found ${reviews.size} past reviews for user $userId on establishment $establishmentId")

            val lastReview = reviews.maxByOrNull { it.timestamp }
            if (lastReview != null) {
                Timber.d("⏱ Last review timestamp: ${lastReview.timestamp} (${Date(lastReview.timestamp)})")
            } else {
                Timber.d("⏱ No previous reviews found for this user on this establishment")
            }

            val now = System.currentTimeMillis()
            val canReviewByTime = lastReview == null || (now - lastReview.timestamp) > 30 * 60 * 1000
            Timber.d("⏳ Current time: $now (${Date(now)})")
            Timber.d("⏳ Time since last review: ${if (lastReview != null) now - lastReview.timestamp else "N/A"} ms")
            Timber.d("✅ Can review by time? $canReviewByTime")

            // Fetch establishment location
            val estSnapshot = firestore.collection("establishments")
                .document(establishmentId)
                .get()
                .await()
            val est = estSnapshot.toObject(Establishment::class.java)
            Timber.d("📍 Establishment location: lat=${est?.latitude}, lng=${est?.longitude}")

            val estLocation = LatLng(est?.latitude ?: 0.0, est?.longitude ?: 0.0)

            // Calculate distance
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                estLocation.latitude, estLocation.longitude,
                results
            )
            val distance = results[0]
            val canReviewByDistance = distance < 50
            Timber.d("📏 Distance between user and establishment: $distance meters")
            Timber.d("✅ Can review by distance? $canReviewByDistance")

            val finalDecision = canReviewByTime && canReviewByDistance
            Timber.d("🎯 Final decision: $finalDecision")

            finalDecision
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking if user can review")
            false
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
