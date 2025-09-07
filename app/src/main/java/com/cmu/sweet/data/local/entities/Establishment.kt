package com.cmu.sweet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Data class representing an establishment entity in the local database.
 * This class is annotated with @Entity to define the table name as "establishments".
 * It includes fields for the establishment's ID, name, address, type, description, latitude,
 * longitude, and the user who added it.
 * @see Establishment
 *
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["addedBy"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["addedBy"])],
    tableName = "establishments"
)
@IgnoreExtraProperties
data class Establishment(
    @PrimaryKey var id: String,
    var name: String,
    var address: String,
    var type: String,
    var description: String,
    var latitude: Double,
    var longitude: Double,
    var addedBy: String,
    val createdAt: Long? = null
) {
    /**
     * Default constructor required by Room.
     *
     */
    constructor() : this("", "", "", "", 0.0, 0.0, "")

    /**
     * Parameterized constructor for easy instantiation.
     * @param name Name of the establishment.
     * @param address Address of the establishment.
     * @param type Type or category of the establishment (e.g., restaurant, cafe).
     * @param description Description of the establishment.
     * @param latitude Geographical latitude of the establishment.
     * @param longitude Geographical longitude of the establishment.
     * @param addedBy ID of the user who added the establishment.
     * The id is auto-generated as a random UUID string.
     * @see Establishment
     *
     */
    constructor(
        name: String,
        address: String,
        type: String,
        description: String,
        latitude: Double,
        longitude: Double,
        addedBy: String
    ) : this(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        address = address,
        type = type,
        description = description,
        latitude = latitude,
        longitude = longitude,
        addedBy = addedBy
    )
}




