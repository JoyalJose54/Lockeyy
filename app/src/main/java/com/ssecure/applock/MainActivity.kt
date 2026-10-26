package com.ssecure.applock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.ui.apps.AppListScreen
import com.ssecure.applock.ui.dashboard.DashboardScreen
import com.ssecure.applock.ui.settings.LockTypeSetupScreen

class MainActivity : ComponentActivity() {
    private lateinit var prefs: AppLockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppLockPreferences.getInstance(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    AppNavigation(prefs = prefs)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(prefs: AppLockPreferences) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable("dashboard") {
            DashboardScreen(
                prefs = prefs,
                onNavigateToSettings = {
                    navController.navigate("lockSettings")
                }
            )
        }

        composable("lockSettings") {
            com.ssecure.applock.ui.settings.LockSettingsScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() },
                onNavigateToLockType = { navController.navigate("lockType") },
                onNavigateToLockedApps = { navController.navigate("appList") }
            )
        }

        composable("appList") {
            AppListScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() }
            )
        }

        composable("lockType") {
            LockTypeSetupScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
