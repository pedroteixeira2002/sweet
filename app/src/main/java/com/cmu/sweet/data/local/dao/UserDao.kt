package com.cmu.sweet.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cmu.sweet.data.local.entities.User
import com.cmu.sweet.data.local.entities.relations.UserWithEstablishments
import com.cmu.sweet.data.local.entities.relations.UserWithReviews

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)

    @Query("SELECT * FROM users")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getById(userId: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getByIdSuspend(userId: String): User?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithEstablishments(userId: String): LiveData<UserWithEstablishments>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithReviews(userId: String): LiveData<UserWithReviews>
}
