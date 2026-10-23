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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                    navController.navigate("lockAndMaskSettings")
                }
            )
        }

        composable("lockAndMaskSettings") {
            com.ssecure.applock.ui.settings.LockAndMaskSettingsScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() },
                onNavigateToLockType = { navController.navigate("lockType") },
                onNavigateToLockedApps = { navController.navigate("appList/false") },
                onNavigateToMaskedApps = { navController.navigate("appList/true") }
            )
        }

        composable(
            route = "appList/{isMasked}",
            arguments = listOf(navArgument("isMasked") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isMasked = backStackEntry.arguments?.getBoolean("isMasked") ?: false
            AppListScreen(
                prefs = prefs,
                isMaskedMode = isMasked,
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
