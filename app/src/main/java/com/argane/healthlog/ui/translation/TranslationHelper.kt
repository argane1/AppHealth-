package com.argane.healthlog.ui.translation

import android.content.Context
import java.util.Locale

object TranslationHelper {
    private val translations = mapOf(
        "en" to mapOf(
            "app_name" to "Daily Symptom Log",
            "tab_log" to "Log Daily",
            "tab_history" to "Calendar",
            "tab_report" to "Report"
        ),
        "ar" to mapOf(
            "app_name" to "سجل الأعراض اليومية",
            "tab_log" to "تسجيل يومي",
            "tab_history" to "التقويم",
            "tab_report" to "التقرير"
        )
    )

    fun translate(key: String, locale: Locale): String {
        val languageCode = if (locale.language == "ar") "ar" else "en"
        return translations[languageCode]?.getOrDefault(key, key) ?: key
    }
}