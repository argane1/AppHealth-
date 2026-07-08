package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_logs")
data class SymptomLogEntry(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val moodRating: Int,         // 1 to 5 (1 = Terrible, 5 = Excellent)
    val symptoms: List<String>,  // Picked symptoms, stored via TypeConverter
    val diet: String,            // What they ate
    val sleepHours: Float,       // Sleep duration in hours
    val sleepQuality: Int,       // 1 to 5 (1 = Poor, 5 = Excellent)
    val notes: String,            // General optional notes
    val pulse: Int? = null,
    val bloodGlucose: Float? = null,
    val weight: Float? = null
)
