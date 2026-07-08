package com.argane.healthlog.data

import androidx.room.TypeConverter

class SymptomLogConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").filter { it.isNotEmpty() }
    }
}