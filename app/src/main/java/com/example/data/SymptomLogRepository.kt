package com.example.data

import androidx.room.RoomDatabase
import com.argane.healthlog.data.SymptomLogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Repository interface for symptom log operations.
 * Defines the contract that can be mocked in unit tests or replaced with a fake implementation.
 */
interface SymptomLogRepository {

    /** Returns all symptom logs ordered by date (newest first). */
    fun getAllLogs(): Flow<List<SymptomLogEntry>>

    /** Returns the log entry for the given date, or null if not found. */
    fun getLogByDate(date: String): Flow<SymptomLogEntry?>

    /** Inserts a new symptom log entry. Emits a success/error result. */
    suspend fun insertLog(entry: SymptomLogEntry): InsertResult

    /** Deletes an existing symptom log entry by date. */
    suspend fun deleteLog(date: String): DeleteResult

    /** Returns the last inserted log ID, or null if none exists yet. */
    fun getLastInsertedId(): Flow<Long?>

    /** Refreshes cached data and returns all entries. */
    suspend fun refreshAllLogs(): List<SymptomLogEntry>
}

/** Result of an insert operation: either success with the new entry or failure with a message. */
sealed class InsertResult {
    data object Success : InsertResult()
    data class Failure(val message: String) : InsertResult()
}

/** Result of a delete operation: either success or failure with a message. */
sealed class DeleteResult {
    data object Success : DeleteResult()
    data class Failure(val message: String) : DeleteResult()
}

/**
 * Production implementation backed by Room DAO.
 * Wraps the DAO cleanly so that UI code never touches Room directly, and supports dependency injection.
 */
class SymptomLogRepositoryImpl(
    private val symptomLogDao: SymptomLogDao,
    /** Optional database instance for cache/refresh operations. Can be null to skip refresh. */
    private val db: RoomDatabase? = null
) : SymptomLogRepository {

    override fun getAllLogs(): Flow<List<SymptomLogEntry>> = symptomLogDao.getAllLogs()

    override fun getLogByDate(date: String): Flow<SymptomLogEntry?> = symptomLogDao.getLogByDate(date)

    override suspend fun insertLog(entry: SymptomLogEntry): InsertResult =
        try {
            val id = symptomLogDao.insertLog(entry)
            if (id == -1L) {
                InsertResult.Failure("Failed to insert log entry")
            } else {
                InsertResult.Success
            }
        } catch (e: Exception) {
            InsertResult.Failure(e.message ?: "Unknown error while inserting log entry")
        }

    override suspend fun deleteLog(date: String): DeleteResult =
        try {
            val affected = symptomLogDao.deleteLog(date)
            if (affected == 0) {
                DeleteResult.Failure("No entry found with date $date")
            } else {
                DeleteResult.Success
            }
        } catch (e: Exception) {
            DeleteResult.Failure(e.message ?: "Unknown error while deleting log entry")
        }

    override fun getLastInsertedId(): Flow<Long?> = symptomLogDao.getLastInsertedId()

    override suspend fun refreshAllLogs(): List<SymptomLogEntry> {
        if (db == null) return emptyList()
        // In a real app, we'd open an in-memory database for caching.
        // For now this is a placeholder that delegates back to the DAO.
        @Suppress("UNCHECKED_CAST")
        val all = symptomLogDao.getAllLogs().firstOrNull() as? List<SymptomLogEntry> ?: emptyList()
        return all
    }

    /**
     * Factory function used by Hilt / manual DI wiring.
     */
    class Factory {
        operator fun invoke(
            symptomLogDao: SymptomLogDao,
            db: RoomDatabase? = null
        ) = SymptomLogRepositoryImpl(symptomLogDao, db)
    }

    companion object {
        /** Returns a singleton factory for dependency injection. */
        @Volatile
        private var INSTANCE: Factory? = null

        fun getInstance(): Factory {
            val instance = INSTANCE
            if (instance != null) return instance
            synchronized(this) {
                val newOne = Factory()
                INSTANCE = newOne
                return newOne
            }
        }
    }
}

/** Convenience extension to turn a [Flow] into a list, useful for one-time reads. */
suspend fun <T> Flow<T>.toList(): List<T> {
    val builder = mutableListOf<T>()
    collect { builder.add(it) }
    return builder.toList()
}