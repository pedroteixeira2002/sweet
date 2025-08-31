package com.cmu.sweet.data.local.repository

import com.cmu.sweet.data.local.dao.EstablishmentDao
import com.cmu.sweet.data.local.entities.EstablishmentEntity
import com.cmu.sweet.utils.haversineDistance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.cos

class EstablishmentRepository(
    private val dao: EstablishmentDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Add a new establishment with the provided details.
     * Generates a unique ID for the establishment.
     * Saves to Firestore and caches locally in Room.
     * Returns Result.success(Unit) on success, or Result.failure(exception) on error.
     * @param name Name of the establishment
     * @param address Address of the establishment
     * @param description Description of the establishment
     * @param type Type/category of the establishment
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Result<Unit>
     *
     */
    suspend fun addEstablishment(
        name: String,
        address: String,
        description: String,
        type: String,
        latitude: Double,
        longitude: Double,
        addedBy: String
    ): Result<Unit> {
        return try {
            val id = firestore.collection("establishments").document().id
            val data = EstablishmentEntity(id, name, address, type, description, latitude, longitude, addedBy)

            firestore.collection("establishments").document(id).set(data).await()

            dao.insert(data)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all establishments (Room first, Firestore fallback)
     * @return List of EstablishmentEntity
     *
      */
    suspend fun getAll(): List<EstablishmentEntity> {
        val localData = dao.getAll()
        if (localData.isNotEmpty()) return localData

        // Fetch from Firestore if local empty
        val snapshot = firestore.collection("establishments").get().await()
        val remoteData = snapshot.documents.mapNotNull { it.toObject(EstablishmentEntity::class.java) }
        remoteData.forEach { dao.insert(it) } // Cache locally
        return remoteData
    }

    /**
     * Get establishment by ID (Room first, Firestore fallback)
     * @param id Establishment ID
     * @return EstablishmentEntity or null if not found
     *
     */
    suspend fun getById(id: String): EstablishmentEntity? {
        dao.getById(id)?.let { return it } // Local first
        val doc = firestore.collection("establishments").document(id).get().await()
        return doc.toObject(EstablishmentEntity::class.java)?.also { dao.insert(it) }
    }

    /**
     * Fetch establishments within a radius from a center point.
     * Uses bounding box for initial Firestore query, then filters with Haversine formula.
     * @param centerLat Center latitude
     * @param centerLng Center longitude
     * @param radiusMeters Radius in meters
     * @return List of EstablishmentEntity within the radius
     *
     */
    suspend fun fetchNearbyEstablishments(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Double
    ): List<EstablishmentEntity> {
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

        val all = snapshot.toObjects(EstablishmentEntity::class.java)

        return all.filter {
            haversineDistance(centerLat, centerLng, it.latitude, it.longitude) <= radiusMeters
        }
    }
}
