package com.cmu.sweet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cmu.sweet.data.local.entities.EstablishmentEntity

/**
 * Data Access Object (DAO) for the EstablishmentEntity.
 * Provides methods to perform database operations related to establishments.
 * Includes methods to retrieve all establishments, retrieve establishments by user ID,
 * retrieve a specific establishment by its ID, and insert new establishments.
 * Handles conflicts by replacing existing entries with the same primary key.
 * This interface is used by Room to generate the necessary code for database interactions.
 * @see  EstablishmentEntity
 * @Dao annotation indicates that this interface is a DAO component in Room.
 *
 */
@Dao
interface EstablishmentDao {
    /**
     * Retrieves all establishments from the database.
     * @return A list of all EstablishmentEntity objects.
     * suspend function to support asynchronous operations.
     * @Query annotation defines the SQL query to be executed.
     * The query selects all columns from the "establishments" table.
     *
     */
    @Query("SELECT * FROM establishments")
    suspend fun getAll(): List<EstablishmentEntity>

    /**
     * Retrieves establishments added by a specific user.
     * @param userId The ID of the user whose establishments are to be retrieved.
     * @return A list of EstablishmentEntity objects added by the specified user.
     * suspend function to support asynchronous operations.
     * @Query annotation defines the SQL query to be executed.
     * The query selects all columns from the "establishments" table where the "addedBy" column matches the provided userId.
     *
     */
    @Query("SELECT * FROM establishments WHERE addedBy = :userId")
    suspend fun getByUserId(userId: String): List<EstablishmentEntity>

    /**
     * Retrieves a specific establishment by its ID.
     * @param id The ID of the establishment to be retrieved.
     * @return An EstablishmentEntity object if found, or null if no matching establishment exists
     * suspend function to support asynchronous operations.
     * @Query annotation defines the SQL query to be executed.
     * The query selects all columns from the "establishments" table where the "id" column matches the provided id.
     *
     */
    @Query("SELECT * FROM establishments WHERE id = :id")
    suspend fun getById(id: String): EstablishmentEntity?

    /**
     * Inserts a new establishment into the database.
     * If an establishment with the same ID already exists, it will be replaced.
     * @param establishment The EstablishmentEntity object to be inserted.
     * suspend function to support asynchronous operations.
     * @Insert annotation indicates that this method is used for inserting data into the database.
     * onConflict = OnConflictStrategy.REPLACE specifies that in case of a conflict (e.g., duplicate primary key), the existing entry should be replaced with the new one.
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(establishment: EstablishmentEntity)

}
