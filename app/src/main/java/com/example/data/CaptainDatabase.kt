package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CaptainProfile::class, TripLog::class, ShiftSettings::class],
    version = 1,
    exportSchema = false
)
abstract class CaptainDatabase : RoomDatabase() {
    abstract fun captainDao(): CaptainDao

    companion object {
        @Volatile
        private var INSTANCE: CaptainDatabase? = null

        fun getDatabase(context: Context): CaptainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaptainDatabase::class.java,
                    "captain_sync_db"
                )
                .fallbackToDestructiveMigration() // safe for rapid prototyping updates
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
