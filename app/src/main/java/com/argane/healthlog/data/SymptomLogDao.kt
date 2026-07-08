package com.argane.healthlog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomLogDao {
    @Query("SELECT * FROM symptom_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<SymptomLogEntry>>

    @Query("SELECT * FROM symptom_logs WHERE date = :date LIMIT 1")
    fun getLogByDate(date: String): Flow<SymptomLogEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: SymptomLogEntry)

    @Delete
    suspend fun deleteLog(entry: SymptomLogEntry)
}