package com.cmu.sweet.data.remote.dto

/**
 * Firestore DTO for Review.
 * Used exclusively for Firestore read/write operations.
 */
data class ReviewDto(
    val id: String = "",
    val establishmentId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val priceRating: Int = 0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
