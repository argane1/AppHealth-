package com.argane.healthlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SymptomLogEntry::class], version = 2, exportSchema = false)
@TypeConverters(SymptomLogConverters::class)
abstract class SymptomLogDatabase : RoomDatabase() {
    abstract fun symptomLogDao(): SymptomLogDao

    companion object {
        @Volatile
        private var INSTANCE: SymptomLogDatabase? = null

        fun getDatabase(context: Context): SymptomLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SymptomLogDatabase::class.java,
                    "symptom_log_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}