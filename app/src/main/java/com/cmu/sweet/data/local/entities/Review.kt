package com.cmu.sweet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing a review left by a user for an establishment.
 * Fields:
 * - id: Unique identifier for the review.
 * - establishmentId: ID of the establishment being reviewed.
 * - userId: ID of the user who wrote the review.
 * - rating: Numerical rating given by the user (e.g., 1-5).
 * - comment: Textual comment provided by the user.
 * - timestamp: Time when the review was created.
 * - photoUrls: List of URLs pointing to photos associated with the review.
 * Note: Room does not support storing lists directly; a TypeConverter is needed for photoUrls.
 *
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Establishment::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["establishmentId"]), Index(value = ["userId"])],
    tableName = "reviews"
)
data class Review(
    @PrimaryKey var id: String,
    var establishmentId: String,
    var userId: String,
    var rating: Int,
    var priceRating: Int,
    var comment: String,
    var timestamp: Long
) {

    /**
     * Default constructor required by Room.
     */
    constructor(
    ) : this("", "", "", 0, 0, "", 0L)

    constructor(
        id: String,
        establishmentId: String,
        userId: String,
        rating: Int,
        priceRating: Int,
        comment: String,
    ) : this(
        id = id,
        establishmentId = establishmentId,
        userId = userId,
        rating = rating,
        priceRating = priceRating,
        comment = comment,
        timestamp = System.currentTimeMillis(),
    )
}