package com.cmu.sweet.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.relations.EstablishmentWithReviews

/**
 * Data Access Object (DAO) for the EstablishmentEntity.
 * Provides methods to perform database operations related to establishments.
 * Includes methods to retrieve all establishments, retrieve establishments by user ID,
 * retrieve a specific establishment by its ID, and insert new establishments.
 * Handles conflicts by replacing existing entries with the same primary key.
 * This interface is used by Room to generate the necessary code for database interactions.
 * @see  Establishment
 * @Dao annotation indicates that this interface is a DAO component in Room.
 *
 */
@Dao
interface EstablishmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(establishment: Establishment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(establishments: List<Establishment>)

    @Delete
    suspend fun delete(establishment: Establishment)

    @Update
    suspend fun update(establishment: Establishment)

    @Query("SELECT * FROM establishments")
    fun getAll(): LiveData<List<Establishment>>

    @Query("SELECT * FROM establishments WHERE addedBy = :userId")
    fun getByUserId(userId: String): LiveData<List<Establishment>>

    @Query("SELECT * FROM establishments WHERE id = :id")
    fun getById(id: String): LiveData<Establishment?>

    @Transaction
    @Query("SELECT * FROM establishments WHERE id = :id")
    fun getEstablishmentWithReviews(id: String): LiveData<EstablishmentWithReviews>
}
