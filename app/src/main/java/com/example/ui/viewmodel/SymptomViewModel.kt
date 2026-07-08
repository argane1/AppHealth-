package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SymptomLogDatabase
import com.example.data.SymptomLogEntry
import com.example.data.SymptomLogRepository
import com.example.ui.translation.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SymptomViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SymptomLogRepository
    
    val allLogs: StateFlow<List<SymptomLogEntry>>
    
    var currentLanguage by mutableStateOf(AppLanguage.ENGLISH)
        private set
    
    var selectedDate by mutableStateOf("")
        private set
        
    var selectedDateEntry by mutableStateOf<SymptomLogEntry?>(null)
        private set

    var glucoseUnit by mutableStateOf("mg/dL")
        private set

    var weightUnit by mutableStateOf("kg")
        private set

    private var activeCollectJob: Job? = null

    init {
        // Read settings from SharedPreferences
        val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLangCode = prefs.getString("selected_language", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        currentLanguage = AppLanguage.fromCode(savedLangCode)
        glucoseUnit = prefs.getString("glucose_unit", "mg/dL") ?: "mg/dL"
        weightUnit = prefs.getString("weight_unit", "kg") ?: "kg"

        val database = SymptomLogDatabase.getDatabase(application)
        repository = SymptomLogRepository(database.symptomLogDao())
        allLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        // Set selected date to today by default
        val todayStr = getTodayDateString()
        selectDate(todayStr)
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        val prefs = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", language.code).apply()
    }

    fun updateGlucoseUnit(unit: String) {
        glucoseUnit = unit
        val prefs = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("glucose_unit", unit).apply()
    }

    fun updateWeightUnit(unit: String) {
        weightUnit = unit
        val prefs = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("weight_unit", unit).apply()
    }

    fun selectDate(dateString: String) {
        selectedDate = dateString
        activeCollectJob?.cancel()
        activeCollectJob = viewModelScope.launch {
            repository.getLogByDate(dateString).collect { entry ->
                selectedDateEntry = entry
            }
        }
    }

    fun saveLog(
        moodRating: Int,
        symptoms: List<String>,
        diet: String,
        sleepHours: Float,
        sleepQuality: Int,
        notes: String,
        pulse: Int? = null,
        bloodGlucose: Float? = null,
        weight: Float? = null
    ) {
        viewModelScope.launch {
            val entry = SymptomLogEntry(
                date = selectedDate,
                moodRating = moodRating,
                symptoms = symptoms,
                diet = diet,
                sleepHours = sleepHours,
                sleepQuality = sleepQuality,
                notes = notes,
                pulse = pulse,
                bloodGlucose = bloodGlucose,
                weight = weight
            )
            repository.insertLog(entry)
        }
    }

    // Returns 1 for up, -1 for down, 0 for equal/no change, null for no previous entry
    fun getWeightTrend(currentDate: String, currentWeight: Float?): Int? {
        if (currentWeight == null) return null
        val logs = allLogs.value.sortedBy { it.date } // sort ascending to find chronologically previous
        val currentIndex = logs.indexOfFirst { it.date == currentDate }
        
        val prevEntryWithWeight = if (currentIndex <= 0) {
            // Either not found, or it is the first recorded day
            logs.filter { it.date < currentDate && it.weight != null }
                .maxByOrNull { it.date }
        } else {
            logs.subList(0, currentIndex)
                .filter { it.weight != null }
                .maxByOrNull { it.date }
        }

        if (prevEntryWithWeight != null && prevEntryWithWeight.weight != null) {
            val diff = currentWeight - prevEntryWithWeight.weight
            return when {
                diff > 0.01f -> 1
                diff < -0.01f -> -1
                else -> 0
            }
        }
        return null
    }

    fun deleteLog(entry: SymptomLogEntry) {
        viewModelScope.launch {
            repository.deleteLog(entry)
            // If the deleted log was the currently selected date, it will automatically update in selectedDateEntry via Flow collector
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper to format date into a human readable string with dynamic locale support
    fun formatHumanDate(dateStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(dateStr) ?: return dateStr
            val targetLocale = Locale(currentLanguage.code)
            val formatter = SimpleDateFormat("EEEE, d MMM, yyyy", targetLocale)
            formatter.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}
