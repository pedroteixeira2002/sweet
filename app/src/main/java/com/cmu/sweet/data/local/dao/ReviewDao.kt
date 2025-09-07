package com.cmu.sweet.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.local.entities.relations.ReviewWithUserAndEstablishment

/**
 * Data Access Object for the ReviewEntity.
 * Provides methods to interact with the reviews table in the database.
 * Includes methods to get reviews by user ID, establishment ID, insert new reviews, and delete reviews by ID.
 * All methods are suspend functions to support asynchronous operations.
 * @see Review
 *
 */
@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews WHERE userId = :userId")
    fun getByUserId(userId: String): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE establishmentId = :establishmentId")
    fun getByEstablishmentId(establishmentId: String): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE id = :id")
    fun getById(id: String): LiveData<Review?>

    @Query("SELECT * FROM reviews")
    fun getAll(): LiveData<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<Review>)

    @Delete
    suspend fun delete(review: Review)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    @Query("SELECT * FROM reviews WHERE id = :id")
    fun getReviewWithUserAndEstablishment(id: String): LiveData<ReviewWithUserAndEstablishment>
}
