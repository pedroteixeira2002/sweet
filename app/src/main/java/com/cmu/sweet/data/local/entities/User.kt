package com.cmu.sweet.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a user entity in the local database.
 * This class is annotated with @Entity to define the table name as "users".
 * It includes fields for the user's ID, name, email, bio, and the timestamp of when the user was created.
 * @see User
 *
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String ,
    var name: String ,
    var email: String ,
    var bio: String?,
    var createdAt: Long?
) {

    /**
     * Default constructor required by Room.
     *
     */
    constructor() : this("", "", "", null, null)

    /** Overloaded constructor without bio parameter.
     * @param id Unique identifier for the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * The bio is set to null by default.
     * The createdAt timestamp is set to null by default and can be set later.
     * @see User
     *
     */
    constructor(name: String, email: String) : this(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        email = email,
        bio = null,
        createdAt = null
    )

    /**
     * Fully parameterized constructor including ID.
     * @param id Unique identifier for the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * The bio is set to null by default.
     * The createdAt timestamp is set to null by default and can be set later.
     * @see User
     *
     */
    constructor(id: String, name: String, email: String) : this(
        id = id,
        name = name,
        email = email,
        bio = null,
        createdAt = null
    )

}