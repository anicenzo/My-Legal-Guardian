package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentEntity::class,
        ClauseEntity::class,
        MissingProtectionEntity::class,
        NegotiationDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contractDao(): ContractDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "legal_guardian_database"
                )
                // Only permit destructive migration in debug builds for development iteration.
                // In production, user vault data is protected from silent wipes on schema changes.
                if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    builder.fallbackToDestructiveMigration(dropAllTables = true)
                }
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
