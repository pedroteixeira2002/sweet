package com.cmu.sweet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.internal.JavaVersion

/**
 * Data class representing a user entity in the local database.
 * This class is annotated with @Entity to define the table name as "users".
 * It includes fields for the user's ID, name, email, bio, and the timestamp of when the user was created.
 * @see UserEntity
 *
 */
@Entity(tableName = "users")
class UserEntity {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var email: String = ""
    var bio: String? = null
    var createdAt: Long? = null

    /**
     * Default constructor required by Room.
     *
     */
    constructor()

    /**
     * Parameterized constructor for easy instantiation.
     * @param id Unique identifier for the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * @param bio Optional biography or description of the user.
     * The createdAt timestamp is set to null by default and can be set later.
     * @see UserEntity
     *
     */
    constructor(name: String, email: String, bio: String?) {
        this.id = java.util.UUID.randomUUID().toString()
        this.name = name
        this.email = email
        this.bio = bio
        this.createdAt = null
    }

    /** Overloaded constructor without bio parameter.
     * @param id Unique identifier for the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * The bio is set to null by default.
     * The createdAt timestamp is set to null by default and can be set later.
     * @see UserEntity
     *
     */
    constructor(name: String, email: String) {
        this.id = java.util.UUID.randomUUID().toString()
        this.name = name
        this.email = email
        this.bio = null
        this.createdAt = null
    }

    /**
     * Fully parameterized constructor including ID.
     * @param id Unique identifier for the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * The bio is set to null by default.
     * The createdAt timestamp is set to null by default and can be set later.
     * @see UserEntity
     *
     */
    constructor(id: String, name: String, email: String) {
        this.id = id
        this.name = name
        this.email = email
        this.bio = null
        this.createdAt = null
    }
}