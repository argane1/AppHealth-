package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CalmingTealPrimary,
    secondary = CalmingLavender,
    tertiary = CalmingTealMedium,
    background = Color(0xFF121E1C),
    surface = Color(0xFF1A2A27),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CalmingTealPrimary,
    secondary = CalmingLavender,
    tertiary = CalmingTealMedium,
    background = WarmWhiteBackground,
    surface = SoftCardWhite,
    onPrimary = Color.White,
    onSecondary = DeepTealText,
    onTertiary = Color.White,
    onBackground = DeepTealText,
    onSurface = DeepTealText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to strictly enforce our Clean Minimalism brand palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
