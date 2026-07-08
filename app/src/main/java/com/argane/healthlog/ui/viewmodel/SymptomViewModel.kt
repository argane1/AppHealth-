package com.argane.healthlog.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.argane.healthlog.data.SymptomLogDatabase
import com.argane.healthlog.data.SymptomLogEntry
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SymptomViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SymptomLogDatabase.getDatabase(application)
    private var currentLanguage = java.util.Locale.getDefault()

    fun setLanguage(languageCode: String) {
        currentLanguage = if (languageCode == "ar") Locale("ar") else Locale.getDefault()
    }

    fun getCurrentLanguage(): java.util.Locale = currentLanguage

    val isRtl get() = currentLanguage.isRtl

    fun addLogEntry(entry: SymptomLogEntry) {
        viewModelScope.launch {
            database.symptomLogDao().insertLog(entry)
        }
    }

    fun getAllLogs() = viewModelScope.launch {
        database.symptomLogDao().getAllLogs()
    }

    override fun onCleared() {
        super.onCleared()
    }
}