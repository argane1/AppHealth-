package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SymptomLogEntry
import com.example.ui.theme.*
import com.example.ui.translation.AppLanguage
import com.example.ui.translation.TranslationHelper
import com.example.ui.viewmodel.SymptomViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: SymptomViewModel,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val allLogs by viewModel.allLogs.collectAsState()
    val currentLanguage = viewModel.currentLanguage

    // Keep track of the active calendar view month and year
    var calendarYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-based

    // Map logs for fast lookup by date: "YYYY-MM-DD" -> SymptomLogEntry
    val logsMap = remember(allLogs) {
        allLogs.associateBy { it.date }
    }

    // Prepare Calendar Grid days
    val calendarInstance = remember(calendarYear, calendarMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, calendarYear)
            set(Calendar.MONTH, calendarMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
    val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val monthName = remember(calendarMonth, currentLanguage) {
        val formatter = SimpleDateFormat("MMMM", Locale(currentLanguage.code))
        val tempCal = Calendar.getInstance().apply { set(Calendar.MONTH, calendarMonth) }
        formatter.format(tempCal.time)
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
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calendar Tracker Icon",
                tint = CalmingTealMedium,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = TranslationHelper.translate("past_log_history", currentLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CalmingTealPrimary,
                    fontSize = 20.sp,
                    letterSpacing = (-0.2).sp
                )
            )
        }

        // CALENDAR MONTH SELECTOR CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (calendarMonth == 0) {
                                calendarMonth = 11
                                calendarYear--
                            } else {
                                calendarMonth--
                            }
                        },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "$monthName $calendarYear",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        )
                    )

                    IconButton(
                        onClick = {
                            if (calendarMonth == 11) {
                                calendarMonth = 0
                                calendarYear++
                            } else {
                                calendarMonth++
                            }
                        },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = CalmingTealMedium,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of week header labels (S, M, T, W, T, F, S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = remember(currentLanguage) {
                        val symbols = java.text.DateFormatSymbols(Locale(currentLanguage.code))
                        val shortWeekdays = symbols.shortWeekdays // index 1 is Sunday, index 7 is Saturday
                        listOf(
                            shortWeekdays[1].take(2),
                            shortWeekdays[2].take(2),
                            shortWeekdays[3].take(2),
                            shortWeekdays[4].take(2),
                            shortWeekdays[5].take(2),
                            shortWeekdays[6].take(2),
                            shortWeekdays[7].take(2)
                        )
                    }
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateMuted
                             ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days grid logic
                val totalCells = 42 // 6 rows of 7 days
                var currentDay = 1

                for (row in 0 until 6) {
                    if (currentDay > daysInMonth) break
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 1..7) {
                            val cellIndex = row * 7 + col
                            val isDayInMonth = cellIndex >= firstDayOfWeek && currentDay <= daysInMonth
                            
                            if (isDayInMonth) {
                                val dayNum = currentDay
                                val dateStr = String.format(Locale.US, "%d-%02d-%02d", calendarYear, calendarMonth + 1, dayNum)
                                val hasLog = logsMap.containsKey(dateStr)
                                val log = logsMap[dateStr]
                                val isSelected = viewModel.selectedDate == dateStr

                                // Color code backings based on mood rating
                                val backgroundColor = when {
                                    isSelected -> CalmingTealLight
                                    hasLog && log != null -> {
                                        when (log.moodRating) {
                                            4, 5 -> PastelGreenGood.copy(alpha = 0.5f)
                                            3 -> PastelYellowNeutral.copy(alpha = 0.5f)
                                            else -> PastelRedBad.copy(alpha = 0.5f)
                                        }
                                    }
                                    else -> Color.Transparent
                                }

                                val borderModifier = if (isSelected) {
                                    Modifier.border(2.dp, CalmingTealMedium, CircleShape)
                                } else {
                                    Modifier
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(backgroundColor)
                                        .then(borderModifier)
                                        .clickable {
                                            viewModel.selectDate(dateStr)
                                        }
                                        .testTag("calendar_day_$dayNum"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || hasLog) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected || hasLog) CalmingTealPrimary else SoftGrayText,
                                                fontSize = 13.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        if (hasLog && log != null && !isSelected) {
                                            // tiny accent bullet to show mood emoji
                                            Text(
                                                text = when (log.moodRating) {
                                                    5 -> "🤩"
                                                    4 -> "🙂"
                                                    3 -> "😐"
                                                    2 -> "😟"
                                                    else -> "😔"
                                                },
                                                fontSize = 8.sp,
                                                lineHeight = 10.sp
                                            )
                                        }
                                    }
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SELECTION DETAIL CARD BELOW CALENDAR
        val selectedDateStr = viewModel.selectedDate
        val selectedLog = viewModel.selectedDateEntry

        Text(
            text = TranslationHelper.translate("selected_day_details", currentLanguage) + " - " + viewModel.formatHumanDate(selectedDateStr),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CalmingTealPrimary,
                letterSpacing = (-0.2).sp
            ),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
        )

        AnimatedVisibility(
            visible = selectedLog != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (selectedLog != null) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                        .testTag("log_details_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Header showing mood
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val moodLabelOnly = when (selectedLog.moodRating) {
                                5 -> "Excellent"
                                4 -> "Good"
                                3 -> "Okay"
                                2 -> "Poor"
                                else -> "Terrible"
                            }
                            val moodEmoji = when (selectedLog.moodRating) {
                                5 -> "🤩"
                                4 -> "🙂"
                                3 -> "😐"
                                2 -> "😟"
                                else -> "😔"
                            }
                            val moodText = "$moodEmoji " + TranslationHelper.translateMood(moodLabelOnly, currentLanguage)
                            
                            val moodBgColor = when (selectedLog.moodRating) {
                                4, 5 -> PastelGreenGood.copy(alpha = 0.3f)
                                3 -> PastelYellowNeutral.copy(alpha = 0.3f)
                                else -> PastelRedBad.copy(alpha = 0.3f)
                            }
                            
                            Text(
                                text = TranslationHelper.translate("mood_short", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = SoftGrayText
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(moodBgColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = moodText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealPrimary
                                    )
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                        // Symptoms experienced
                        Text(
                            text = TranslationHelper.translate("symptoms_short", currentLanguage),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CalmingTealPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedLog.symptoms.isEmpty()) {
                            Text(
                                text = TranslationHelper.translate("congrats_no_symptoms", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SoftGrayText,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedLog.symptoms.forEach { symptom ->
                                    val emoji = when(symptom) {
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(CalmingTealLight)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$emoji " + TranslationHelper.translateSymptom(symptom, currentLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealMedium
                                        )
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                        // Sleep Metrics
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = "Sleep",
                                        tint = CalmingTealMedium,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = TranslationHelper.translate("sleep_duration", currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftGrayText)
                                    )
                                }
                                Text(
                                    text = "${selectedLog.sleepHours} " + TranslationHelper.translate("hours_label", currentLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealPrimary
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = TranslationHelper.translate("sleep_quality", currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = SoftGrayText)
                                )
                                val qualityLabelOnly = when(selectedLog.sleepQuality) {
                                    5 -> "Excellent"
                                    4 -> "Good"
                                    3 -> "Fair"
                                    2 -> "Restless"
                                    else -> "Poor"
                                }
                                val qualityEmoji = when(selectedLog.sleepQuality) {
                                    5 -> "😴"
                                    4 -> "😌"
                                    3 -> "😐"
                                    2 -> "🥱"
                                    else -> "😫"
                                }
                                val qualityLabel = "$qualityEmoji " + TranslationHelper.translateSleepQuality(qualityLabelOnly, currentLanguage)
                                Text(
                                    text = qualityLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealPrimary
                                    )
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                        // Nutrition Diet Log
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Diet Icon",
                                tint = CalmingTealMedium,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = TranslationHelper.translate("logged_diet", currentLanguage),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CalmingTealPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedLog.diet.isEmpty()) TranslationHelper.translate("no_log_found", currentLanguage) else selectedLog.diet,
                            style = MaterialTheme.typography.bodyMedium.copy(color = SoftGrayText)
                        )

                        // Vitals & Metrics details in Day Details card
                        val hasVitals = selectedLog.pulse != null || selectedLog.bloodGlucose != null || selectedLog.weight != null
                        if (hasVitals) {
                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Vitals Icon",
                                    tint = CalmingTealMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = TranslationHelper.translate("vitals_metrics_title", currentLanguage),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedLog.pulse?.let { pulse ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CalmingTealLight.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = "❤️", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = TranslationHelper.translate("pulse_heart_rate", currentLanguage) + ":",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SoftGrayText
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "$pulse bpm",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealMedium
                                        )
                                    }
                                }
                                selectedLog.bloodGlucose?.let { glucose ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CalmingTealLight.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = "🩸", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = TranslationHelper.translate("blood_glucose", currentLanguage) + ":",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SoftGrayText
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "$glucose ${viewModel.glucoseUnit}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealMedium
                                        )
                                    }
                                }
                                selectedLog.weight?.let { weight ->
                                    val weightTrend = viewModel.getWeightTrend(selectedLog.date, weight)
                                    val arrow = when (weightTrend) {
                                        1 -> " ▲"
                                        -1 -> " ▼"
                                        else -> ""
                                    }
                                    val trendColor = when (weightTrend) {
                                        1 -> Color(0xFFE57373)
                                        -1 -> Color(0xFF81C784)
                                        else -> SoftGrayText
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CalmingTealLight.copy(alpha = 0.5f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = "⚖️", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = TranslationHelper.translate("body_weight", currentLanguage) + ":",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SoftGrayText
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "$weight ${viewModel.weightUnit}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CalmingTealMedium
                                        )
                                        if (arrow.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = arrow,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = trendColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedLog.notes.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Notes",
                                    tint = CalmingTealMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = TranslationHelper.translate("logged_notes", currentLanguage),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CalmingTealPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedLog.notes,
                                style = MaterialTheme.typography.bodyMedium.copy(color = SoftGrayText)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // EDIT ENTRY BUTTON
                        OutlinedButton(
                            onClick = onNavigateToLog,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CalmingTealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("edit_selected_log_button")
                        ) {
                            Text(
                                text = TranslationHelper.translate("update_entry", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        if (selectedLog == null) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = CalmingTealLight.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CalmingTealLight, RoundedCornerShape(32.dp))
                    .testTag("empty_selected_log_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "☕ " + TranslationHelper.translate("no_log_found", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = TranslationHelper.translate("click_below_to_log", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SoftGrayText,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToLog,
                        colors = ButtonDefaults.buttonColors(containerColor = CalmingTealPrimary),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_entry_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = TranslationHelper.translate("save_entry", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                             )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PREFERENCES & SETTINGS CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp))
                .testTag("settings_card"),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("language_settings", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = TranslationHelper.translate("select_language", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAFAF9))
                        .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("language_dropdown_trigger")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLanguage.flag + "  " + currentLanguage.displayName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CalmingTealPrimary
                            )
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown Indicator",
                            tint = CalmingTealMedium
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color.White)
                    ) {
                        AppLanguage.values().forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = language.flag, fontSize = 18.sp, modifier = Modifier.padding(end = 12.dp))
                                        Text(
                                            text = language.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                                color = if (language == currentLanguage) CalmingTealMedium else Color.DarkGray
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setLanguage(language)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Blood Glucose Unit Selection
                Text(
                    text = TranslationHelper.translate("blood_sugar_unit", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("mg/dL", "mmol/L").forEach { unit ->
                        val isSelected = viewModel.glucoseUnit == unit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CalmingTealLight else Color(0xFFFAFAF9))
                                .border(1.dp, if (isSelected) CalmingTealMedium else Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                                .clickable { viewModel.updateGlucoseUnit(unit) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CalmingTealMedium else SoftGrayText
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight Unit Selection
                Text(
                    text = TranslationHelper.translate("weight_unit", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("kg", "lbs").forEach { unit ->
                        val isSelected = viewModel.weightUnit == unit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CalmingTealLight else Color(0xFFFAFAF9))
                                .border(1.dp, if (isSelected) CalmingTealMedium else Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                                .clickable { viewModel.updateWeightUnit(unit) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CalmingTealMedium else SoftGrayText
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
