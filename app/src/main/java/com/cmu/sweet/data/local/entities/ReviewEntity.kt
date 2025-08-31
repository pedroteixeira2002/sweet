package com.cmu.sweet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
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
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)

data class ReviewEntity(
    @PrimaryKey var id: String,
    var establishmentId: String,
    var userId: String,
    var rating: Int,
    var comment: String,
    var timestamp: Long,
    var photoUrls: List<String>,
) {

    /**
     * Default constructor required by Room.
     */
    constructor(
    ) : this("", "", "", 0, "", 0L, emptyList())

    /**
     * Parameterized constructor for easy instantiation.
     * @param id Unique identifier for the review.
     * @param establishmentId ID of the establishment being reviewed.
     * @param userId ID of the user who wrote the review.
     * @param rating Numerical rating given by the user (e.g., 1-5).
     * @param comment Textual comment provided by the user.
     * @param timestamp Time when the review was created.
     * @param photoUrls List of URLs pointing to photos associated with the review.
     * @see ReviewEntity
     */
    constructor(
        establishmentId: String,
        userId: String,
        rating: Int,
        comment: String,
        photoUrls: List<String>
    ) : this(
        id = java.util.UUID.randomUUID().toString(),
        establishmentId = establishmentId,
        userId = userId,
        rating = rating,
        comment = comment,
        timestamp = System.currentTimeMillis(),
        photoUrls = photoUrls
    )

    /**
     * Overloaded constructor without photoUrls parameter.
     * @param establishmentId ID of the establishment being reviewed.
     * @param userId ID of the user who wrote the review.
     * @param rating Numerical rating given by the user (e.g., 1-5).
     * @param comment Textual comment provided by the user.
     * The photoUrls list is set to an empty list by default.
     * @see ReviewEntity
     */
    constructor(
        establishmentId: String,
        userId: String,
        rating: Int,
        comment: String,
    ) : this(
        id = java.util.UUID.randomUUID().toString(),
        establishmentId = establishmentId,
        userId = userId,
        rating = rating,
        comment = comment,
        timestamp = System.currentTimeMillis(),
        photoUrls = emptyList()
    )
}