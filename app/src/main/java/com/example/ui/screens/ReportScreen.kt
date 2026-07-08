package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.SymptomLogEntry
import com.example.receiver.ReminderHelper
import com.example.ui.theme.*
import com.example.ui.translation.AppLanguage
import com.example.ui.translation.TranslationHelper
import com.example.ui.viewmodel.SymptomViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class PatternInsight(
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: SymptomViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val allLogs by viewModel.allLogs.collectAsState()
    val currentLanguage = viewModel.currentLanguage

    // SharedPreferences for Daily Reminders
    val prefs = remember { context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE) }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean("reminder_enabled", false)) }
    var reminderHour by remember { mutableIntStateOf(prefs.getInt("reminder_hour", 19)) }
    var reminderMinute by remember { mutableIntStateOf(prefs.getInt("reminder_minute", 30)) }

    // Alarm permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
            prefs.edit().putBoolean("reminder_enabled", true).apply()
            ReminderHelper.scheduleDailyReminder(context, reminderHour, reminderMinute)
            Toast.makeText(context, TranslationHelper.translate("reminder_enabled_toast", currentLanguage), Toast.LENGTH_SHORT).show()
        } else {
            reminderEnabled = false
            prefs.edit().putBoolean("reminder_enabled", false).apply()
            Toast.makeText(context, TranslationHelper.translate("perm_req", currentLanguage), Toast.LENGTH_SHORT).show()
        }
    }

    // Time picker dialog
    val timePickerDialog = remember(currentLanguage, reminderHour, reminderMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minuteOfHour ->
                reminderHour = hourOfDay
                reminderMinute = minuteOfHour
                prefs.edit().putInt("reminder_hour", hourOfDay).putInt("reminder_minute", minuteOfHour).apply()
                if (reminderEnabled) {
                    ReminderHelper.scheduleDailyReminder(context, hourOfDay, minuteOfHour)
                }
            },
            reminderHour,
            reminderMinute,
            false
        )
    }

    // Perform real-time report analysis calculations
    val totalDays = allLogs.size
    val averageMood = remember(allLogs) {
        if (allLogs.isEmpty()) 0.0f
        else allLogs.map { it.moodRating }.average().toFloat()
    }
    
    val averageSleep = remember(allLogs) {
        if (allLogs.isEmpty()) 0.0f
        else allLogs.map { it.sleepHours }.average().toFloat()
    }

    val symptomFrequencies = remember(allLogs) {
        val frequencies = mutableMapOf<String, Int>()
        allLogs.flatMap { it.symptoms }.forEach { symptom ->
            frequencies[symptom] = (frequencies[symptom] ?: 0) + 1
        }
        frequencies.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(WarmWhiteBackground)
            .padding(18.dp)
    ) {
        // App Title Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 14.dp, top = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "Doctor Summary Icon",
                tint = CalmingTealMedium,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = TranslationHelper.translate("doctor_report_panel", currentLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CalmingTealPrimary,
                    fontSize = 20.sp,
                    letterSpacing = (-0.2).sp
                )
            )
        }

        // HEALING GRAPHICS HEADER
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CalmingTealLight.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CalmingTealLight, RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "📋 " + TranslationHelper.translate("patient_health_dossier", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CalmingTealPrimary,
                        letterSpacing = (-0.2).sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = TranslationHelper.translate("patient_dossier_desc", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SoftGrayText,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DAILY REMINDER SETTINGS CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                .testTag("reminder_settings_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Reminder",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("daily_logging_reminder", currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CalmingTealPrimary,
                                    letterSpacing = (-0.2).sp
                                )
                            )
                            Text(
                                text = if (reminderEnabled) {
                                    TranslationHelper.translate("scheduled_for", currentLanguage) + " " + formatTime(reminderHour, reminderMinute, currentLanguage)
                                } else {
                                    TranslationHelper.translate("reminders_off", currentLanguage)
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SoftGrayText
                                )
                            )
                        }
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { value ->
                            if (value) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasPermission) {
                                        reminderEnabled = true
                                        prefs.edit().putBoolean("reminder_enabled", true).apply()
                                        ReminderHelper.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                        Toast.makeText(context, TranslationHelper.translate("reminder_enabled_toast", currentLanguage), Toast.LENGTH_SHORT).show()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    reminderEnabled = true
                                    prefs.edit().putBoolean("reminder_enabled", true).apply()
                                    ReminderHelper.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                    Toast.makeText(context, TranslationHelper.translate("reminder_enabled_toast", currentLanguage), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                reminderEnabled = false
                                prefs.edit().putBoolean("reminder_enabled", false).apply()
                                ReminderHelper.cancelDailyReminder(context)
                                Toast.makeText(context, TranslationHelper.translate("reminder_disabled_toast", currentLanguage), Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CalmingTealMedium,
                            uncheckedThumbColor = Color(0xFFCCCCCC),
                            uncheckedTrackColor = Color(0xFFEEEEEE)
                        ),
                        modifier = Modifier.testTag("reminder_enable_switch")
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { timePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = CalmingTealLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_reminder_time_button")
                    ) {
                        Text(
                            text = TranslationHelper.translate("change_time", currentLanguage) + " (${formatTime(reminderHour, reminderMinute, currentLanguage)})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CalmingTealPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allLogs.isEmpty()) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                    .testTag("empty_stats_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📈 " + TranslationHelper.translate("not_enough_data", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = TranslationHelper.translate("not_enough_data_desc", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SoftGrayText,
                            textAlign = TextAlign.Center
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // STATS TILES GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mood Stats Card
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                        .testTag("mood_stats_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Mood",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TranslationHelper.translate("avg_mood", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(color = SoftGrayText)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format(Locale(currentLanguage.code), "%.1f", averageMood),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CalmingTealPrimary
                                )
                            )
                            Text(
                                text = "/5",
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftGrayText)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val moodDesc = when {
                            averageMood >= 4.0f -> "🤩 " + TranslationHelper.translate("healthy", currentLanguage)
                            averageMood >= 3.0f -> "😐 " + TranslationHelper.translate("stable", currentLanguage)
                            else -> "😔 " + TranslationHelper.translate("low", currentLanguage)
                        }
                        Text(
                            text = moodDesc,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CalmingTealMedium
                            )
                        )
                    }
                }

                // Sleep Stats Card
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                        .testTag("sleep_stats_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep",
                            tint = SleepText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TranslationHelper.translate("avg_sleep", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(color = SoftGrayText)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format(Locale(currentLanguage.code), "%.1f", averageSleep),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CalmingTealPrimary
                                )
                            )
                            Text(
                                text = TranslationHelper.translate("hours_label", currentLanguage),
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftGrayText)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val sleepDesc = when {
                            averageSleep >= 7.0f -> "😴 " + TranslationHelper.translate("sufficient", currentLanguage)
                            averageSleep >= 5.0f -> "🥱 " + TranslationHelper.translate("restless", currentLanguage)
                            else -> "😫 " + TranslationHelper.translate("low", currentLanguage)
                        }
                        Text(
                            text = sleepDesc,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SleepText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MOST FREQUENT SYMPTOMS
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                    .testTag("symptoms_freq_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Medical",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = TranslationHelper.translate("primary_symptom_freq", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CalmingTealPrimary,
                                letterSpacing = (-0.2).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (symptomFrequencies.isEmpty()) {
                        Text(
                            text = TranslationHelper.translate("congrats_no_symptoms", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SoftGrayText,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    } else {
                        symptomFrequencies.take(4).forEachIndexed { index, pair ->
                            val symptomName = pair.first
                            val count = pair.second
                            val emoji = when (symptomName) {
                                "Headache" -> "🤕"
                                "Fatigue" -> "🥱"
                                "Nausea" -> "🤢"
                                "Fever" -> "🤒"
                                "Stomachache" -> "😣"
                                "Muscle Pain" -> "🤕"
                                "Anxiety / Stress" -> "😰"
                                "Cough" -> "😷"
                                "Dizziness" -> "🌀"
                                "Insomnia" -> "👁️"
                                else -> "💊"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}.",
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealMedium,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(text = emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = TranslationHelper.translateSymptom(symptomName, currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealPrimary
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CalmingTealLight)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (count == 1) {
                                            TranslationHelper.translate("one_log", currentLanguage)
                                        } else {
                                            String.format(Locale(currentLanguage.code), TranslationHelper.translate("n_logs", currentLanguage), count)
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CLINICAL WELLNESS PATTERNS
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                    .testTag("patterns_insights_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Patterns",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = TranslationHelper.translate("wellness_patterns", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CalmingTealPrimary,
                                letterSpacing = (-0.2).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    val patternInsights = remember(allLogs, currentLanguage) { getPatternInsights(allLogs, currentLanguage) }
                    patternInsights.forEach { insight ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = insight.icon,
                                contentDescription = "Insight Icon",
                                tint = insight.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = insight.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DeepTealText,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // EXPORT PDF FOR DOCTOR BUTTON
            Button(
                onClick = {
                    exportAndSharePdf(context, allLogs, averageMood, averageSleep, symptomFrequencies, currentLanguage, viewModel.glucoseUnit, viewModel.weightUnit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalmingTealPrimary),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = TranslationHelper.translate("export_pdf", currentLanguage),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SHARE TEXT REPORT BUTTON
            OutlinedButton(
                onClick = {
                    shareReport(context, allLogs, averageMood, averageSleep, symptomFrequencies, currentLanguage, viewModel.glucoseUnit, viewModel.weightUnit)
                },
                border = BorderStroke(1.5.dp, CalmingTealMedium),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_report_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = CalmingTealMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = TranslationHelper.translate("share_text", currentLanguage),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CalmingTealMedium,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Format time in AM/PM for user-friendly display
fun formatTime(hour: Int, minute: Int, lang: AppLanguage): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale(lang.code)).format(calendar.time)
}

// Generate pattern insights based on patient data logs
fun getPatternInsights(logs: List<SymptomLogEntry>, lang: AppLanguage): List<PatternInsight> {
    val insights = mutableListOf<PatternInsight>()
    if (logs.isEmpty()) {
        insights.add(
            PatternInsight(
                TranslationHelper.translate("no_logged_data_insight", lang),
                Icons.Default.Info,
                CalmingTealMedium
            )
        )
        return insights
    }

    // 1. Check for specific symptom frequency in the last 7 logged days
    val recentLogs = logs.sortedByDescending { it.date }.take(7)
    val symptomCounts = mutableMapOf<String, Int>()
    recentLogs.flatMap { it.symptoms }.forEach { sym ->
        symptomCounts[sym] = (symptomCounts[sym] ?: 0) + 1
    }
    
    val topSymptom = symptomCounts.maxByOrNull { it.value }
    if (topSymptom != null && topSymptom.value >= 3) {
        val formatStr = TranslationHelper.translate("recent_symptom_insight", lang)
        val symptomTranslated = TranslationHelper.translateSymptom(topSymptom.key, lang)
        insights.add(
            PatternInsight(
                String.format(Locale(lang.code), formatStr, symptomTranslated, topSymptom.value, recentLogs.size),
                Icons.Default.TrendingUp,
                Color(0xFFE57373) // Soft Red
            )
        )
    }

    // 2. Correlation: Poor sleep quality / short sleep duration overlapping with symptoms
    val poorSleepDays = logs.filter { it.sleepQuality <= 2 || it.sleepHours < 6.0f }
    if (poorSleepDays.isNotEmpty()) {
        val poorSleepSymptoms = poorSleepDays.flatMap { it.symptoms }
        val mostCommonPoorSleepSymptom = poorSleepSymptoms.groupBy { it }
            .mapValues { it.value.size }
            .maxByOrNull { it.value }
        if (mostCommonPoorSleepSymptom != null && mostCommonPoorSleepSymptom.value >= 2) {
            val formatStr = TranslationHelper.translate("poor_sleep_insight", lang)
            val symptomTranslated = TranslationHelper.translateSymptom(mostCommonPoorSleepSymptom.key, lang)
            insights.add(
                PatternInsight(
                    String.format(Locale(lang.code), formatStr, symptomTranslated, mostCommonPoorSleepSymptom.value),
                    Icons.Default.Bedtime,
                    Color(0xFF9575CD) // Lavender Purple
                )
            )
        }
    }

    // 3. Positive correlation: High sleep duration linked to better feeling/mood ratings
    val goodSleepDays = logs.filter { it.sleepHours >= 7.5f }
    if (goodSleepDays.isNotEmpty()) {
        val avgMoodOnGoodSleep = goodSleepDays.map { it.moodRating }.average().toFloat()
        val overallAvgMood = logs.map { it.moodRating }.average().toFloat()
        if (avgMoodOnGoodSleep > overallAvgMood + 0.3f) {
            val formatStr = TranslationHelper.translate("good_sleep_insight", lang)
            insights.add(
                PatternInsight(
                    String.format(Locale(lang.code), formatStr, avgMoodOnGoodSleep),
                    Icons.Default.Star,
                    Color(0xFFFFB74D) // Amber Gold
                )
            )
        }
    }

    // 4. General fallback tip if logs are few or no strong correlations found
    if (insights.size < 2) {
        val avgSleep = logs.map { it.sleepHours }.average().toFloat()
        if (avgSleep < 7.0f) {
            val formatStr = TranslationHelper.translate("low_sleep_tip", lang)
            insights.add(
                PatternInsight(
                    String.format(Locale(lang.code), formatStr, avgSleep),
                    Icons.Default.Info,
                    Color(0xFF4FC3F7) // Soft Sky Blue
                )
            )
        } else {
            insights.add(
                PatternInsight(
                    TranslationHelper.translate("consistency_tip", lang),
                    Icons.Default.Info,
                    CalmingTealMedium
                )
            )
        }
    }

    return insights
}

// PDF Generation & secure sharing action for consultations
private fun exportAndSharePdf(
    context: Context,
    logs: List<SymptomLogEntry>,
    averageMood: Float,
    averageSleep: Float,
    frequencies: List<Pair<String, Int>>,
    lang: AppLanguage,
    glucoseUnit: String,
    weightUnit: String
) {
    try {
        val document = PdfDocument()
        
        // Define page width and height for A4 (595 x 842 pt)
        val pageWidth = 595
        val pageHeight = 842
        
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        // Setup Paints
        val paintText = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        
        val paintTitle = Paint().apply {
            color = android.graphics.Color.rgb(0x1F, 0x4C, 0x4C) // Deep Teal
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val paintSubtitle = Paint().apply {
            color = android.graphics.Color.rgb(0x4D, 0x82, 0x82) // Medium Teal
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintHeading = Paint().apply {
            color = android.graphics.Color.rgb(0x1F, 0x4C, 0x4C)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintBorder = Paint().apply {
            color = android.graphics.Color.rgb(0xE0, 0xE0, 0xE0)
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val paintFill = Paint().apply {
            color = android.graphics.Color.rgb(0xF4, 0xFA, 0xFA) // Light Teal Background tint
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var y = 50f
        
        fun checkPageOverflow(lineHeight: Float) {
            if (y + lineHeight > 780f) {
                document.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
        }

        // Draw Header Box
        canvas.drawRect(30f, 30f, (pageWidth - 30).toFloat(), 95f, paintFill)
        canvas.drawRect(30f, 30f, (pageWidth - 30).toFloat(), 95f, paintBorder)
        
        canvas.drawText(TranslationHelper.translate("dossier_title", lang), 45f, 58f, paintTitle)
        
        val dateRangeStr = if (logs.isNotEmpty()) {
            val sorted = logs.sortedBy { it.date }
            TranslationHelper.translate("reporting_period", lang) + ": ${sorted.first().date} " + TranslationHelper.translate("cancel", lang).lowercase() + " ${sorted.last().date}"
        } else {
            TranslationHelper.translate("no_logs_registered", lang)
        }
        canvas.drawText(dateRangeStr, 45f, 78f, paintSubtitle)
        
        val genDateStr = TranslationHelper.translate("generated_on", lang) + ": " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale(lang.code)).format(Date())
        val genDateWidth = paintText.measureText(genDateStr)
        canvas.drawText(genDateStr, pageWidth - 45f - genDateWidth, 78f, paintText)
        
        y = 120f
        
        // Section: Aggregates
        checkPageOverflow(80f)
        canvas.drawText(TranslationHelper.translate("clinical_aggregates", lang), 40f, y, paintHeading)
        y += 18f
        canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, paintBorder)
        y += 20f
        
        canvas.drawText(TranslationHelper.translate("total_days_recorded", lang) + ": ${logs.size}", 50f, y, paintText)
        y += 18f
        canvas.drawText(String.format(Locale(lang.code), TranslationHelper.translate("average_mood_rating", lang) + ": %.2f / 5.0", averageMood), 50f, y, paintText)
        y += 18f
        canvas.drawText(String.format(Locale(lang.code), TranslationHelper.translate("average_sleep_duration", lang) + ": %.2f " + TranslationHelper.translate("hours_label", lang), averageSleep), 50f, y, paintText)
        y += 30f

        // Section: Wellness Patterns
        checkPageOverflow(100f)
        canvas.drawText(TranslationHelper.translate("clinical_wellness_trends", lang), 40f, y, paintHeading)
        y += 18f
        canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, paintBorder)
        y += 20f
        
        val patternList = getPatternInsights(logs, lang)
        patternList.forEach { insight ->
            checkPageOverflow(22f)
            canvas.drawText("• " + insight.text, 50f, y, paintText)
            y += 18f
        }
        y += 15f

        // Section: Top Symptoms
        checkPageOverflow(100f)
        canvas.drawText(TranslationHelper.translate("prevalent_symptoms", lang), 40f, y, paintHeading)
        y += 18f
        canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, paintBorder)
        y += 20f
        
        if (frequencies.isEmpty()) {
            canvas.drawText(TranslationHelper.translate("no_symptoms_reported", lang), 50f, y, paintText)
            y += 18f
        } else {
            frequencies.take(5).forEach { (symptom, count) ->
                checkPageOverflow(22f)
                val symptomName = TranslationHelper.translateSymptom(symptom, lang)
                val freqLabel = if (count == 1) {
                    TranslationHelper.translate("one_log", lang)
                } else {
                    String.format(Locale(lang.code), TranslationHelper.translate("n_logs", lang), count)
                }
                canvas.drawText("• $symptomName: $freqLabel", 50f, y, paintText)
                y += 18f
            }
        }
        y += 30f

        // Section: Chronological Daily Logs
        checkPageOverflow(100f)
        canvas.drawText(TranslationHelper.translate("chronological_journal", lang), 40f, y, paintHeading)
        y += 18f
        canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, paintBorder)
        y += 25f
        
        logs.sortedByDescending { it.date }.forEach { log ->
            checkPageOverflow(105f)
            
            // Draw a box for each log
            val boxTop = y - 12f
            val boxBottom = y + 72f
            canvas.drawRect(45f, boxTop, (pageWidth - 45).toFloat(), boxBottom, paintFill)
            canvas.drawRect(45f, boxTop, (pageWidth - 45).toFloat(), boxBottom, paintBorder)
            
            // Log Header
            paintSubtitle.textSize = 9f
            canvas.drawText("DATE: ${log.date}", 55f, y, paintSubtitle)
            
            val feelLabelOnly = when (log.moodRating) {
                5 -> "Excellent"
                4 -> "Good"
                3 -> "Okay"
                2 -> "Poor"
                else -> "Terrible"
            }
            val moodLabel = TranslationHelper.translateMood(feelLabelOnly, lang) + " (${log.moodRating}/5)"
            canvas.drawText(TranslationHelper.translate("mood_short", lang) + ": $moodLabel", 220f, y, paintText)
            y += 16f
            
            val qualityLabelOnly = when (log.sleepQuality) {
                5 -> "Excellent"
                4 -> "Good"
                3 -> "Fair"
                2 -> "Restless"
                else -> "Poor"
            }
            val qualityTranslated = TranslationHelper.translateSleepQuality(qualityLabelOnly, lang)
            canvas.drawText(TranslationHelper.translate("sleep_short", lang) + ": ${log.sleepHours} " + TranslationHelper.translate("hours_label", lang) + " (${TranslationHelper.translate("sleep_quality", lang)}: $qualityTranslated - ${log.sleepQuality}/5)", 55f, y, paintText)
            y += 16f
            
            val symptomsStr = if (log.symptoms.isEmpty()) {
                TranslationHelper.translate("congrats_no_symptoms", lang)
            } else {
                log.symptoms.map { TranslationHelper.translateSymptom(it, lang) }.joinToString(", ")
            }
            canvas.drawText(TranslationHelper.translate("symptoms_short", lang) + ": $symptomsStr", 55f, y, paintText)
            y += 16f
            
            // Vitals Line
            val pulseText = if (log.pulse != null) "${log.pulse} bpm" else "—"
            val glucoseText = if (log.bloodGlucose != null) "${log.bloodGlucose} $glucoseUnit" else "—"
            val weightText = if (log.weight != null) "${log.weight} $weightUnit" else "—"
            canvas.drawText("Pulse: $pulseText | Glucose: $glucoseText | Weight: $weightText", 55f, y, paintText)
            y += 16f

            val notesStr = if (log.notes.isEmpty()) TranslationHelper.translate("no_log_found", lang) else log.notes
            val dietStr = if (log.diet.isEmpty()) TranslationHelper.translate("no_log_found", lang) else log.diet
            canvas.drawText(TranslationHelper.translate("logged_diet", lang) + ": $dietStr | " + TranslationHelper.translate("logged_notes", lang) + ": $notesStr", 55f, y, paintText)
            
            y += 32f // spacing between cards
        }
        
        // Finish final page
        document.finishPage(page)
        
        // Write PDF file to context cache securely
        val file = File(context.cacheDir, "Symptom_Log_Doctor_Report.pdf")
        val outputStream = FileOutputStream(file)
        document.writeTo(outputStream)
        document.close()
        outputStream.close()
        
        // Share PDF via Android FileProvider Intent
        val authority = "com.example.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, TranslationHelper.translate("dear_doctor_pdf_subject", lang))
            putExtra(Intent.EXTRA_TEXT, TranslationHelper.translate("dear_doctor_pdf_body", lang))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, TranslationHelper.translate("export_clinical_pdf", lang))
        context.startActivity(chooser)
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, TranslationHelper.translate("failed_generate_pdf", lang) + ": ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

// Simple text-based sharing fallback
private fun shareReport(
    context: Context,
    logs: List<SymptomLogEntry>,
    avgMood: Float,
    avgSleep: Float,
    frequencies: List<Pair<String, Int>>,
    lang: AppLanguage,
    glucoseUnit: String,
    weightUnit: String
) {
    val reportDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale(lang.code)).format(Date())
    
    val sb = StringBuilder()
    sb.append("🏥 " + TranslationHelper.translate("dossier_title", lang) + " 🏥\n")
    sb.append(TranslationHelper.translate("generated_on", lang) + ": $reportDate\n")
    sb.append("=========================================\n\n")
    sb.append(TranslationHelper.translate("dear_doctor_email", lang) + "\n")
    
    sb.append("📊 " + TranslationHelper.translate("patient_stats_section", lang) + ":\n")
    sb.append("- " + TranslationHelper.translate("total_days_recorded", lang) + ": ${logs.size}\n")
    sb.append(String.format(Locale(lang.code), "- " + TranslationHelper.translate("average_mood_rating", lang) + ": %.1f / 5.0\n", avgMood))
    sb.append(String.format(Locale(lang.code), "- " + TranslationHelper.translate("average_sleep_duration", lang) + ": %.1f " + TranslationHelper.translate("hours_label", lang) + "\n", avgSleep))
    
    if (frequencies.isNotEmpty()) {
        sb.append("- " + TranslationHelper.translate("top_reported_symptoms", lang) + ":\n")
        frequencies.take(3).forEach { (symptom, count) ->
            val symptomTranslated = TranslationHelper.translateSymptom(symptom, lang)
            val countLabel = if (count == 1) {
                TranslationHelper.translate("one_log", lang)
            } else {
                String.format(Locale(lang.code), TranslationHelper.translate("n_logs", lang), count)
            }
            sb.append("  * $symptomTranslated ($countLabel)\n")
        }
    } else {
        sb.append("- " + TranslationHelper.translate("no_symptoms_reported", lang) + "\n")
    }
    sb.append("\n=========================================\n\n")
    sb.append("📅 " + TranslationHelper.translate("chronological_logs_section", lang) + ":\n\n")

    logs.sortedBy { it.date }.forEach { log ->
        sb.append("• " + TranslationHelper.translate("today", lang).uppercase() + ": ${log.date}\n")
        val feelLabelOnly = when (log.moodRating) {
            5 -> "Excellent"
            4 -> "Good"
            3 -> "Okay"
            2 -> "Poor"
            else -> "Terrible"
        }
        val feelLabel = TranslationHelper.translateMood(feelLabelOnly, lang) + " (${log.moodRating}/5)"
        sb.append("  * " + TranslationHelper.translate("mood_short", lang) + ": $feelLabel\n")
        
        val symptomsLabel = if (log.symptoms.isEmpty()) {
            TranslationHelper.translate("cancel", lang)
        } else {
            log.symptoms.map { TranslationHelper.translateSymptom(it, lang) }.joinToString(", ")
        }
        sb.append("  * " + TranslationHelper.translate("symptoms_short", lang) + ": $symptomsLabel\n")
        
        val sleepQualityOnly = when (log.sleepQuality) {
            5 -> "Excellent"
            4 -> "Good"
            3 -> "Fair"
            2 -> "Restless"
            else -> "Poor"
        }
        val sleepQualityTranslated = TranslationHelper.translateSleepQuality(sleepQualityOnly, lang)
        sb.append("  * " + TranslationHelper.translate("sleep_short", lang) + ": ${log.sleepHours} " + TranslationHelper.translate("hours_label", lang) + " (${TranslationHelper.translate("sleep_quality", lang)}: $sleepQualityTranslated - ${log.sleepQuality}/5)\n")
        
        val pulseText = if (log.pulse != null) "${log.pulse} bpm" else "—"
        val glucoseText = if (log.bloodGlucose != null) "${log.bloodGlucose} $glucoseUnit" else "—"
        val weightText = if (log.weight != null) "${log.weight} $weightUnit" else "—"
        sb.append("  * Vitals - Pulse: $pulseText | Glucose: $glucoseText | Weight: $weightText\n")
        
        sb.append("  * " + TranslationHelper.translate("logged_diet", lang) + ": " + (if (log.diet.isEmpty()) "None" else log.diet) + "\n")
        if (log.notes.isNotEmpty()) {
            sb.append("  * " + TranslationHelper.translate("extra_notes", lang) + ": ${log.notes}\n")
        }
        sb.append("\n")
    }

    sb.append("=========================================\n")
    sb.append(TranslationHelper.translate("report_ends", lang) + "\n")

    val reportText = sb.toString()

    // Launch share sheet
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, TranslationHelper.translate("app_name", lang) + " " + TranslationHelper.translate("tab_report", lang))
        putExtra(Intent.EXTRA_TEXT, reportText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, TranslationHelper.translate("share_chooser_title", lang))
    context.startActivity(shareIntent)
}
