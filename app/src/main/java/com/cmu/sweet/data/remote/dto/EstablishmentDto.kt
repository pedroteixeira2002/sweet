package com.cmu.sweet.data.remote.dto

/**
 * Firestore DTO for Establishment.
 * Used exclusively for Firestore read/write operations.
 */
data class EstablishmentDto(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val type: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val addedBy: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
