package com.cmu.sweet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cmu.sweet.data.local.dao.EstablishmentDao
import com.cmu.sweet.data.local.dao.ReviewDao
import com.cmu.sweet.data.local.dao.UserDao
import com.cmu.sweet.data.local.entities.Establishment
import com.cmu.sweet.data.local.entities.Review
import com.cmu.sweet.data.local.entities.User
import com.cmu.sweet.data.local.entities.relations.Converter

/**
 * Main Room database for the app.
 * Holds references to all DAOs and entities.
 * Uses TypeConverters for unsupported field types (e.g., List<String> for photoUrls).
 */
@Database(
    entities = [
        User::class,
        Establishment::class,
        Review::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class SweetDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun establishmentDao(): EstablishmentDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: SweetDatabase? = null

        fun getInstance(context: Context): SweetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SweetDatabase::class.java,
                    "sweet_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}