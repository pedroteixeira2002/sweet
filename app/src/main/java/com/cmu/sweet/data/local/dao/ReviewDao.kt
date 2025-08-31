package com.cmu.sweet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.cmu.sweet.data.local.entities.ReviewEntity

/**
 * Data Access Object for the ReviewEntity.
 * Provides methods to interact with the reviews table in the database.
 * Includes methods to get reviews by user ID, establishment ID, insert new reviews, and delete reviews by ID.
 * All methods are suspend functions to support asynchronous operations.
 * @see ReviewEntity
 *
 */
@Dao
interface ReviewDao {
    /**
     * Retrieves a list of reviews made by a specific user.
     * @param userId The ID of the user whose reviews are to be fetched.
     * @return A list of ReviewEntity objects associated with the given user ID.
     * @see ReviewEntity
     *
     */
    @Query ("SELECT * FROM reviews WHERE userId= :userId")
    suspend fun getByUserId(userId: String): List<ReviewEntity>

    /**
     * Retrieves a list of reviews for a specific establishment.
     * @param establishmentId The ID of the establishment whose reviews are to be fetched.
     * @return A list of ReviewEntity objects associated with the given establishment ID.
     * @see ReviewEntity
     *
     */
    @Query("SELECT * FROM reviews WHERE establishmentId = :establishmentId")
    suspend fun getByEstablishmentId(establishmentId: String): List<ReviewEntity>

    /**
     * Inserts a new review into the reviews table.
     * @param id The unique ID of the review.
     * @param establishmentId The ID of the establishment being reviewed.
     * @param userId The ID of the user who made the review.
     * @param rating The rating given in the review.
     * @param comment The comment provided in the review.
     * @see ReviewEntity
     *
     */
    @Query("INSERT INTO reviews (id, establishmentId, userId, rating, comment) VALUES (:id, :establishmentId, :userId, :rating, :comment)")
    suspend fun insert(id: String, establishmentId: String, userId: String, rating: Int, comment: String)

    /**
     * Deletes a review from the reviews table by its ID.
     * @param reviewId The ID of the review to be deleted.
     * @see ReviewEntity
     *
     */
    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteById(reviewId: String)


}