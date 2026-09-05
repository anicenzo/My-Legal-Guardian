package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.engine.ContractType

class Converters {
    @TypeConverter
    fun fromContractType(type: ContractType?): String = (type ?: ContractType.GENERAL_AGREEMENT).name

    @TypeConverter
    fun toContractType(value: String?): ContractType = ContractType.fromString(value)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query("PRAGMA table_info(documents)")
        var hasContractType = false
        while (cursor.moveToNext()) {
            val nameIdx = cursor.getColumnIndex("name")
            if (nameIdx != -1 && cursor.getString(nameIdx) == "contractType") {
                hasContractType = true
                break
            }
        }
        cursor.close()

        if (!hasContractType) {
            db.execSQL("ALTER TABLE documents ADD COLUMN contractType TEXT NOT NULL DEFAULT 'GENERAL_AGREEMENT'")
        } else {
            // Normalize existing legacy string rows to ContractType enum names
            db.execSQL("UPDATE documents SET contractType = 'RESIDENTIAL_LEASE' WHERE contractType = 'lease'")
            db.execSQL("UPDATE documents SET contractType = 'EMPLOYMENT_FREELANCE' WHERE contractType = 'freelance' OR contractType = 'employment'")
            db.execSQL("UPDATE documents SET contractType = 'MEMBERSHIP_SUBSCRIPTION' WHERE contractType = 'subscription' OR contractType = 'membership'")
            db.execSQL("UPDATE documents SET contractType = 'GENERAL_AGREEMENT' WHERE contractType NOT IN ('RESIDENTIAL_LEASE', 'EMPLOYMENT_FREELANCE', 'MEMBERSHIP_SUBSCRIPTION', 'GENERAL_AGREEMENT') OR contractType IS NULL")
        }
    }
}

@Database(
    entities = [
        DocumentEntity::class,
        ClauseEntity::class,
        MissingProtectionEntity::class,
        NegotiationDraftEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                ).addMigrations(MIGRATION_1_2)

                // Only permit destructive migration in debug builds as a last-ditch fallback
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
