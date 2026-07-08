package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.LogEntryScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.theme.*
import com.example.ui.translation.TranslationHelper
import com.example.ui.viewmodel.SymptomViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout()
            }
        }
    }
}

const val ROUTE_LOG = "log"
const val ROUTE_CALENDAR = "calendar"
const val ROUTE_REPORT = "report"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    val navController = rememberNavController()
    val viewModel: SymptomViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_LOG
    val currentLanguage = viewModel.currentLanguage

    // Provide RTL layout direction if language is Arabic
    val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmWhiteBackground),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = TranslationHelper.translate("app_name", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepTealText,
                                letterSpacing = 0.5.sp
                            )
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = WarmWhiteBackground,
                        titleContentColor = DeepTealText
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(width = 1.dp, color = Color(0xFFF0F0F0), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    // Log Daily Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = TranslationHelper.translate("tab_log", currentLanguage)
                            )
                        },
                        label = { Text(TranslationHelper.translate("tab_log", currentLanguage)) },
                        selected = currentRoute == ROUTE_LOG,
                        onClick = {
                            navController.navigate(ROUTE_LOG) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CalmingTealMedium,
                            selectedTextColor = CalmingTealMedium,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted,
                            indicatorColor = CalmingTealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_log")
                    )

                    // History Calendar Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = TranslationHelper.translate("tab_history", currentLanguage)
                            )
                        },
                        label = { Text(TranslationHelper.translate("tab_history", currentLanguage)) },
                        selected = currentRoute == ROUTE_CALENDAR,
                        onClick = {
                            navController.navigate(ROUTE_CALENDAR) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CalmingTealMedium,
                            selectedTextColor = CalmingTealMedium,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted,
                            indicatorColor = CalmingTealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_calendar")
                    )

                    // Report Tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = TranslationHelper.translate("tab_report", currentLanguage)
                            )
                        },
                        label = { Text(TranslationHelper.translate("tab_report", currentLanguage)) },
                        selected = currentRoute == ROUTE_REPORT,
                        onClick = {
                            navController.navigate(ROUTE_REPORT) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CalmingTealMedium,
                            selectedTextColor = CalmingTealMedium,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateMuted,
                            indicatorColor = CalmingTealLight
                        ),
                        modifier = Modifier.testTag("nav_tab_report")
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ROUTE_LOG,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(ROUTE_LOG) {
                    LogEntryScreen(viewModel = viewModel)
                }
                composable(ROUTE_CALENDAR) {
                    CalendarScreen(
                        viewModel = viewModel,
                        onNavigateToLog = {
                            navController.navigate(ROUTE_LOG) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(ROUTE_REPORT) {
                    ReportScreen(viewModel = viewModel)
                }
            }
        }
    }
}
