package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.translation.TranslationHelper
import com.example.ui.viewmodel.SymptomViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogEntryScreen(
    viewModel: SymptomViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Observed ViewModel State
    val selectedDate = viewModel.selectedDate
    val existingEntry = viewModel.selectedDateEntry

    // Local form states (synced with existingEntry via LaunchedEffect)
    var moodRating by remember { mutableIntStateOf(3) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var diet by remember { mutableStateOf("") }
    var sleepHours by remember { mutableFloatStateOf(8.0f) }
    var sleepQuality by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }
    var pulseString by remember { mutableStateOf("") }
    var bloodGlucoseString by remember { mutableStateOf("") }
    var weightString by remember { mutableStateOf("") }

    // Sync form states with database entry when it changes
    LaunchedEffect(existingEntry, selectedDate) {
        if (existingEntry != null) {
            moodRating = existingEntry.moodRating
            selectedSymptoms.clear()
            selectedSymptoms.addAll(existingEntry.symptoms)
            diet = existingEntry.diet
            sleepHours = existingEntry.sleepHours
            sleepQuality = existingEntry.sleepQuality
            notes = existingEntry.notes
            pulseString = existingEntry.pulse?.toString() ?: ""
            bloodGlucoseString = existingEntry.bloodGlucose?.toString() ?: ""
            weightString = existingEntry.weight?.toString() ?: ""
        } else {
            // Reset to defaults for empty logs
            moodRating = 3
            selectedSymptoms.clear()
            diet = ""
            sleepHours = 8.0f
            sleepQuality = 3
            notes = ""
            pulseString = ""
            bloodGlucoseString = ""
            weightString = ""
        }
    }

    // Predefined symptoms list with Emojis and descriptions
    val symptomOptions = listOf(
        SymptomOption("Headache", "🤕"),
        SymptomOption("Fatigue", "🥱"),
        SymptomOption("Nausea", "🤢"),
        SymptomOption("Fever", "🤒"),
        SymptomOption("Stomachache", "😣"),
        SymptomOption("Muscle Pain", "🤕"),
        SymptomOption("Anxiety / Stress", "😰"),
        SymptomOption("Cough", "😷"),
        SymptomOption("Dizziness", "🌀"),
        SymptomOption("Insomnia", "👁️")
    )

    val currentLanguage = viewModel.currentLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(WarmWhiteBackground)
    ) {
        // Clean Minimalism Header Section
        val greeting = remember(currentLanguage) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val key = when (hour) {
                in 0..11 -> "good_morning"
                in 12..16 -> "good_afternoon"
                else -> "good_evening"
            }
            TranslationHelper.translate(key, currentLanguage)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CalmingTealPrimary,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = viewModel.formatHumanDate(selectedDate),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SoftGrayText,
                        fontSize = 14.sp
                    )
                )
            }
            
            // Dynamic User Initials Avatar matching user email: rachidargane@gmail.com -> "RA"
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color = CalmingTealLight, shape = CircleShape)
                    .border(width = 2.dp, color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RA",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = CalmingTealMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date Picker Navigation (Chevron-based back and forth)
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        navigateDay(viewModel, -1)
                    },
                    modifier = Modifier.testTag("prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = TranslationHelper.translate("selected_day", currentLanguage),
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = viewModel.formatHumanDate(selectedDate),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (selectedDate == viewModel.getTodayDateString()) {
                            TranslationHelper.translate("today", currentLanguage)
                        } else {
                            TranslationHelper.translate("selected_day", currentLanguage)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftGrayText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                IconButton(
                    onClick = {
                        navigateDay(viewModel, 1)
                    },
                    modifier = Modifier.testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = TranslationHelper.translate("selected_day", currentLanguage),
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MOOD / FEELING CARD (1-5 Emoji Faces)
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CalmingTealLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = TranslationHelper.translate("feel_question", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CalmingTealMedium,
                        letterSpacing = (-0.2).sp
                    )
                )
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val moods = listOf(
                        MoodItem(1, "😔", "Terrible", PastelRedBad),
                        MoodItem(2, "😟", "Poor", Color(0xFFFFCC80)),
                        MoodItem(3, "😐", "Okay", PastelYellowNeutral),
                        MoodItem(4, "🙂", "Good", Color(0xFFAED581)),
                        MoodItem(5, "🤩", "Excellent", PastelGreenGood)
                    )

                    moods.forEach { mood ->
                        val isSelected = moodRating == mood.rating
                        val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1.0f)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .testTag("mood_button_${mood.rating}")
                                .clickable { moodRating = mood.rating }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 56.dp else 44.dp)
                                    .scale(scale)
                                    .background(
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mood.emoji,
                                    fontSize = if (isSelected) 28.sp else 24.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = TranslationHelper.translateMood(mood.label, currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CalmingTealMedium else SoftGrayText
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SYMPTOMS CHECKLIST CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Symptom Icon",
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("symptoms_question", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Modern wrap/grid layout using FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    symptomOptions.forEach { option ->
                        val isSelected = selectedSymptoms.contains(option.name)
                        
                        Box(
                            modifier = Modifier
                                .testTag("symptom_chip_${option.name.replace(" ", "_")}")
                                .background(
                                    color = if (isSelected) CalmingTealLight else Color(0xFFFAFAF9),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedSymptoms.remove(option.name)
                                    } else {
                                        selectedSymptoms.add(option.name)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = option.emoji,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = TranslationHelper.translateSymptom(option.name, currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CalmingTealMedium else SoftGrayText
                                    )
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = CalmingTealMedium,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SLEEP CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = TranslationHelper.translate("sleep_question", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CalmingTealPrimary,
                        letterSpacing = (-0.2).sp
                    )
                )
                Spacer(modifier = Modifier.height(18.dp))

                // Sleep hours display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = TranslationHelper.translate("sleep_duration", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f %s", sleepHours, TranslationHelper.translate("hours_label", currentLanguage)),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary
                        )
                    )
                }

                // Custom Sleep Duration Slider
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = Math.round(it * 2f) / 2f }, // Snap to nearest 0.5 hours
                    valueRange = 0f..16f,
                    colors = SliderDefaults.colors(
                        thumbColor = CalmingTealPrimary,
                        activeTrackColor = CalmingTealPrimary,
                        inactiveTrackColor = CalmingTealLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sleep_duration_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = TranslationHelper.translate("sleep_quality", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Sleep Quality options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val qualities = listOf(
                        SleepQualityItem(1, "😫", "Poor"),
                        SleepQualityItem(2, "🥱", "Restless"),
                        SleepQualityItem(3, "😐", "Fair"),
                        SleepQualityItem(4, "😌", "Good"),
                        SleepQualityItem(5, "😴", "Excellent")
                    )

                    qualities.forEach { item ->
                        val isQualitySelected = sleepQuality == item.value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sleep_quality_${item.value}")
                                .background(
                                    color = if (isQualitySelected) SleepBg else Color(0xFFFAFAF9),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { sleepQuality = item.value }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = item.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = TranslationHelper.translateSleepQuality(item.label, currentLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = if (isQualitySelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isQualitySelected) SleepText else SoftGrayText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DIET / NUTRITION CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Nutrition Icon",
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("eat_question", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = diet,
                    onValueChange = { diet = it },
                    placeholder = {
                        Text(
                            TranslationHelper.translate("diet_placeholder", currentLanguage),
                            color = SlateMuted,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diet_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalmingTealPrimary,
                        unfocusedBorderColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFFAFAF9),
                        unfocusedContainerColor = Color(0xFFFAFAF9)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                val foodPrompts = listOf("🍳 Breakfast", "🥗 Lunch", "🍲 Dinner", "🍎 Snacks")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    foodPrompts.forEach { prompt ->
                        val label = prompt.substring(2)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CalmingTealLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val prefix = "$label: "
                                    if (diet.isEmpty()) {
                                        diet = prefix
                                    } else {
                                        if (diet.endsWith("\n") || diet.endsWith(" ")) {
                                            diet += prefix
                                        } else {
                                            diet += "\n$prefix"
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CalmingTealMedium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VITALS & METRICS CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Vitals Icon",
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("vitals_metrics_title", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Pulse / Heart Rate
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❤️ " + TranslationHelper.translate("pulse_heart_rate", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                        )
                        OutlinedTextField(
                            value = pulseString,
                            onValueChange = { pulseString = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("72", color = SlateMuted) },
                            suffix = { Text("bpm", fontWeight = FontWeight.Bold, color = CalmingTealMedium) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .testTag("pulse_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalmingTealPrimary,
                                unfocusedBorderColor = Color(0xFFF0F0F0),
                                focusedContainerColor = Color(0xFFFAFAF9),
                                unfocusedContainerColor = Color(0xFFFAFAF9)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TranslationHelper.translate("pulse_resting_note", currentLanguage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftGrayText,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                // 2. Blood Glucose (Sugar) Level
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🩸 " + TranslationHelper.translate("blood_glucose", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                        )
                        OutlinedTextField(
                            value = bloodGlucoseString,
                            onValueChange = { 
                                bloodGlucoseString = it.filter { char -> char.isDigit() || char == '.' } 
                            },
                            placeholder = { Text(if (viewModel.glucoseUnit == "mg/dL") "100" else "5.5", color = SlateMuted) },
                            suffix = { Text(viewModel.glucoseUnit, fontWeight = FontWeight.Bold, color = CalmingTealMedium) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .testTag("glucose_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalmingTealPrimary,
                                unfocusedBorderColor = Color(0xFFF0F0F0),
                                focusedContainerColor = Color(0xFFFAFAF9),
                                unfocusedContainerColor = Color(0xFFFAFAF9)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TranslationHelper.translate("glucose_tracking_only", currentLanguage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SoftGrayText,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                // 3. Weight
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚖️ " + TranslationHelper.translate("body_weight", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            )
                            val weightTrend = viewModel.getWeightTrend(selectedDate, weightString.toFloatOrNull())
                            if (weightTrend != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(start = 24.dp)
                                ) {
                                    val (arrow, label, color) = when (weightTrend) {
                                        1 -> Triple("▲", TranslationHelper.translate("trend_up", currentLanguage), Color(0xFFE57373))
                                        -1 -> Triple("▼", TranslationHelper.translate("trend_down", currentLanguage), Color(0xFF81C784))
                                        else -> Triple("•", TranslationHelper.translate("trend_equal", currentLanguage), SoftGrayText)
                                    }
                                    Text(text = arrow, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = weightString,
                            onValueChange = { 
                                weightString = it.filter { char -> char.isDigit() || char == '.' } 
                            },
                            placeholder = { Text("70.0", color = SlateMuted) },
                            suffix = { Text(viewModel.weightUnit, fontWeight = FontWeight.Bold, color = CalmingTealMedium) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .testTag("weight_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CalmingTealPrimary,
                                unfocusedBorderColor = Color(0xFFF0F0F0),
                                focusedContainerColor = Color(0xFFFAFAF9),
                                unfocusedContainerColor = Color(0xFFFAFAF9)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // EXTRA NOTES CARD
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(32.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Notes Icon",
                        tint = CalmingTealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("notes_label", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CalmingTealPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            TranslationHelper.translate("notes_placeholder", currentLanguage),
                            color = SlateMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalmingTealPrimary,
                        unfocusedBorderColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFFAFAF9),
                        unfocusedContainerColor = Color(0xFFFAFAF9)
                    ),
                    maxLines = 4
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // BIG SAVE BUTTON
        Button(
            onClick = {
                viewModel.saveLog(
                    moodRating = moodRating,
                    symptoms = selectedSymptoms.toList(),
                    diet = diet,
                    sleepHours = sleepHours,
                    sleepQuality = sleepQuality,
                    notes = notes,
                    pulse = pulseString.toIntOrNull(),
                    bloodGlucose = bloodGlucoseString.toFloatOrNull(),
                    weight = weightString.toFloatOrNull()
                )
                val msg = TranslationHelper.translate("save_success", currentLanguage)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CalmingTealPrimary),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp)
                .testTag("save_log_button")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save Icon",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (existingEntry != null) {
                    TranslationHelper.translate("update_entry", currentLanguage)
                } else {
                    TranslationHelper.translate("save_entry", currentLanguage)
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Helpers for navigation
private fun navigateDay(viewModel: SymptomViewModel, daysOffset: Int) {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(viewModel.selectedDate) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        
        // Prevent navigating into the future (which is unlogged)
        val todayCalendar = Calendar.getInstance()
        if (calendar.after(todayCalendar)) {
            return
        }
        
        val newDateStr = sdf.format(calendar.time)
        viewModel.selectDate(newDateStr)
    } catch (e: Exception) {
        // Fallback
    }
}

data class SymptomOption(val name: String, val emoji: String)
data class MoodItem(val rating: Int, val emoji: String, val label: String, val color: Color)
data class SleepQualityItem(val value: Int, val emoji: String, val label: String)
