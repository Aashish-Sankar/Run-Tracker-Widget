package com.marathon.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marathon.tracker.domain.model.ThemeMode
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import com.marathon.tracker.presentation.activities.ActivitiesScreen
import com.marathon.tracker.presentation.auth.AuthViewModel
import com.marathon.tracker.presentation.coaching.CoachingScreen
import com.marathon.tracker.presentation.dashboard.DashboardScreen
import com.marathon.tracker.presentation.plan.PlanManagementScreen
import com.marathon.tracker.presentation.plan.PlanScreen
import com.marathon.tracker.presentation.plan.PlanSetupScreen
import com.marathon.tracker.presentation.settings.SettingsScreen
import com.marathon.tracker.presentation.theme.MarathonTheme
import com.marathon.tracker.presentation.today.TodayScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeMode = runBlocking { preferencesRepository.getThemeMode().first() }
        setContent {
            MarathonTheme(themeMode = themeMode, dynamicColor = true) {
                MarathonApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data ?: return
        if (data.scheme == "marathon" && data.host == "strava") {
            val code = data.getQueryParameter("code") ?: return
            authViewModel.handleStravaCallback(code)
        }
    }
}

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Today : Screen("today", "Today", Icons.Default.Today)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Analytics)
    data object Plan : Screen("plan", "Plan", Icons.Default.CalendarMonth)
    data object Activities : Screen("activities", "Activities", Icons.Default.DirectionsRun)
    data object Coaching : Screen("coaching", "Coaching", Icons.Default.Psychology)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private val bottomNavItems = listOf(
    Screen.Today,
    Screen.Dashboard,
    Screen.Plan,
    Screen.Activities,
    Screen.Coaching,
    Screen.Settings,
)

@Composable
private fun MarathonApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Plan.route) {
                PlanScreen(onManagePlans = { navController.navigate("plan_management") })
            }
            composable("plan_management") {
                PlanManagementScreen(
                    onBack = { navController.popBackStack() },
                    onCreatePlan = { navController.navigate("plan_setup") },
                )
            }
            composable("plan_setup") {
                PlanSetupScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack("plan_management", inclusive = false) },
                )
            }
            composable(Screen.Activities.route) { ActivitiesScreen() }
            composable(Screen.Coaching.route) { CoachingScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
