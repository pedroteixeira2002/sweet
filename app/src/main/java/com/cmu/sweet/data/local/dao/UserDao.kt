package com.cmu.sweet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.cmu.sweet.data.local.entities.UserEntity

/**
 * Data Access Object (DAO) for the UserEntity.
 * This interface defines methods for accessing and manipulating user data in the local database.
 * It includes methods to retrieve all users, get a user by ID, insert a new user, and delete a user by ID.
 * All methods are marked as suspend functions to support asynchronous operations with Kotlin coroutines.
 * The @Dao annotation indicates that this interface is a DAO component in Room.
 * Each method is annotated with @Query to specify the SQL queries to be executed.
 * The UserEntity represents the user data model in the database.
 * This DAO is essential for performing CRUD operations on user data within the application.
 *
 */
@Dao
interface UserDao {
    /**
     * Retrieves all users from the database.
     * @return A list of UserEntity objects representing all users.
     * suspend function for asynchronous operation.
     * @Query annotation specifies the SQL query to be executed.
     */
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>

    /**
     * Retrieves a user by their unique ID.
     * @param userId The unique identifier of the user to be retrieved.
     * @return A UserEntity object representing the user with the specified ID, or null if not found.
     * suspend function for asynchronous operation.
     * @Query annotation specifies the SQL query to be executed.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getById(userId: String): UserEntity?

    /**
     * Inserts a new user into the database.
     * @param id The unique identifier of the user.
     * @param name The name of the user.
     * @param email The email address of the user.
     * suspend function for asynchronous operation.
     * @Query annotation specifies the SQL query to be executed.
     *
     */
    @Query("INSERT INTO users (id, name, email) VALUES (:id, :name, :email)")
    suspend fun insert(id: String, name: String, email: String)

    /**
     * Deletes a user from the database by their unique ID.
     * @param userId The unique identifier of the user to be deleted.
     * suspend function for asynchronous operation.
     * @Query annotation specifies the SQL query to be executed.
     *
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)

    /**
     * Updates an existing user's name and email in the database.
     * @param userId The unique identifier of the user to be updated.
     * @param name The new name of the user.
     * suspend function for asynchronous operation.
     * @Query annotation specifies the SQL query to be executed.
     *
     */
    @Query("UPDATE users SET name = :name WHERE id = :userId")
    suspend fun update(userId: String, name: String, email: String)

}