package com.cmu.sweet.data.local.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cmu.sweet.data.local.dao.EstablishmentDao
import com.cmu.sweet.data.local.entities.EstablishmentEntity

@Database(
    entities = [EstablishmentEntity::class],
    version = 1,
    exportSchema = false
)

abstract class SweetDatabase : RoomDatabase() {
    abstract fun establishmentDao(): EstablishmentDao

    companion object {
        @Volatile
        private var INSTANCE: SweetDatabase? = null

        fun getInstance(context: Context): SweetDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SweetDatabase::class.java,
                    "sweet_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}