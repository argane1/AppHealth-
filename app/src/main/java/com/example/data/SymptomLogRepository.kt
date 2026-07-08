package com.example.data

import kotlinx.coroutines.flow.Flow

class SymptomLogRepository(private val symptomLogDao: SymptomLogDao) {
    val allLogs: Flow<List<SymptomLogEntry>> = symptomLogDao.getAllLogs()

    fun getLogByDate(date: String): Flow<SymptomLogEntry?> = symptomLogDao.getLogByDate(date)

    suspend fun insertLog(entry: SymptomLogEntry) = symptomLogDao.insertLog(entry)

    suspend fun deleteLog(entry: SymptomLogEntry) = symptomLogDao.deleteLog(entry)
}
