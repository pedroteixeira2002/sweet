package com.cmu.sweet.data.repository

import com.cmu.sweet.data.local.dao.EstablishmentDao
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.mappers.toDto
import com.cmu.sweet.data.mappers.toLocal
import com.cmu.sweet.data.remote.dto.EstablishmentDto
import com.cmu.sweet.data.remote.dto.ReviewDto
import com.cmu.sweet.utils.haversineDistance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.math.cos

class EstablishmentRepository(
    private val firestore: FirebaseFirestore,
    private val dao: EstablishmentDao
) {
    companion object { private const val COLLECTION = "establishments" }

    suspend fun syncAll(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION).get().await()
            val dtos = snapshot.toObjects(EstablishmentDto::class.java)
            val entities = dtos.map { it.toLocal() }
            dao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing establishments")
            Result.failure(e)
        }
    }

    suspend fun add(est: Establishment): Result<Unit> {
        return try {
            val dto = est.toDto().copy(id = firestore.collection(COLLECTION).document().id)
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal()) // cache locally
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding establishment")
            Result.failure(e)
        }
    }

    suspend fun update(est: Establishment): Result<Unit> {
        return try {
            val dto = est.toDto()
            firestore.collection(COLLECTION).document(dto.id).set(dto).await()
            dao.insert(dto.toLocal())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating establishment")
            Result.failure(e)
        }
    }

    suspend fun getByUser(userId: String): List<Establishment> {
        val snapshot = firestore.collection(COLLECTION)
            .whereEqualTo("addedBy", userId)
            .get()
            .await()

        return snapshot.toObjects(EstablishmentDto::class.java).map { it.toLocal() }
    }

    suspend fun getReviews(estId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("establishmentId", estId)
                .get()
                .await()
            val dtos = snapshot.toObjects(ReviewDto::class.java)
            val reviews = dtos.map { it.toLocal() }
            Result.success(reviews)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching establishment reviews")
            Result.failure(e)
        }
    }

    suspend fun getRating(estId: String): Result<Double> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("establishmentId", estId)
                .get()
                .await()
            val dtos = snapshot.toObjects(ReviewDto::class.java)
            val reviews = dtos.map { it.toLocal() }
            val average = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average()
            } else 0.0
            Result.success(average)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching establishment rating")
            Result.failure(e)
        }
    }

    suspend fun fetchMyEstablishments(userId: String): Result<List<Establishment>> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("addedBy", userId)
                .get()
                .await()
            val dtos = snapshot.toObjects(EstablishmentDto::class.java)
            val establishments = dtos.map { it.toLocal() }
            dao.insertAll(establishments)
            Result.success(establishments)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user's establishments")
            Result.failure(e)
        }
    }

    suspend fun fetchById(id: String): Result<Establishment?> {
        return try {
            val snapshot = firestore.collection(COLLECTION).document(id).get().await()
            val dto = snapshot.toObject(EstablishmentDto::class.java)
            val est = dto?.toLocal()
            Result.success(est)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAll(): List<Establishment> {
        val snapshot = firestore.collection(COLLECTION).get().await()
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
        return establishments
    }

    suspend fun fetchNearbyEstablishments(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Double
    ): List<Establishment> {
        val latDelta = radiusMeters / 111000.0
        val lngDelta = radiusMeters / (111000.0 * cos(Math.toRadians(centerLat)))

        val minLat = centerLat - latDelta
        val maxLat = centerLat + latDelta
        val minLng = centerLng - lngDelta
        val maxLng = centerLng + lngDelta

        val snapshot = firestore.collection("establishments")
            .whereGreaterThanOrEqualTo("latitude", minLat)
            .whereLessThanOrEqualTo("latitude", maxLat)
            .get()
            .await()

        val dtos = snapshot.toObjects(EstablishmentDto::class.java)

        val entities = dtos.map { dto ->
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

        return entities.filter {
            haversineDistance(centerLat, centerLng, it.latitude, it.longitude) <= radiusMeters
        }
    }
}

