package com.lockeyy.ui.lock

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LockOverlayActivity : androidx.fragment.app.FragmentActivity() {

    @javax.inject.Inject
    lateinit var lockManager: com.lockeyy.core.LockManager

    @javax.inject.Inject
    lateinit var pinManager: com.lockeyy.core.PinManager

    @javax.inject.Inject
    lateinit var settingsManager: com.lockeyy.core.SettingsManager

    private var lockedPackageName: String? = null
    private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
    private lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No transition on enter - we want it instant
        lockedPackageName = intent.getStringExtra("LOCKED_PACKAGE_NAME")

        setupBiometric()

        setContent {
            com.lockeyy.ui.theme.LockeyyTheme {
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                var pinError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                // Fetch App Info Asynchronously to prevent UI lag on startup
                val context = androidx.compose.ui.platform.LocalContext.current
                val infoState = androidx.compose.runtime.produceState(
                    initialValue = "Locked App" to (null as androidx.compose.ui.graphics.ImageBitmap?)
                ) {
                    if (lockedPackageName != null) {
                        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val pm = context.packageManager
                                val info = pm.getApplicationInfo(lockedPackageName!!, 0)
                                val name = pm.getApplicationLabel(info).toString()
                                val icon = pm.getApplicationIcon(info).toBitmap().asImageBitmap()
                                name to icon
                            } catch (e: Exception) {
                                "Locked App" to null
                            }
                        }
                    }
                }
                val (appName, appIcon) = infoState.value

                PinLockScreen(
                    title = "Enter PIN to Unlock",
                    appName = appName,
                    appIcon = appIcon,
                    isError = pinError,
                    onPinChange = { pinError = false },
                    onPinEntered = { pin ->
                        scope.launch {
                            if (pinManager.validatePin(pin)) {
                                unlock()
                            } else {
                                pinError = true
                            }
                        }
                    },
                    onBiometricClick = {
                        attemptBiometricUnlock()
                    }
                )
            }
        }
        
        // Show immediately if savedInstanceState is null (first launch)
        if (savedInstanceState == null) {
            attemptBiometricUnlock()
        }
    }

    override fun onResume() {
        super.onResume()
        // IMPORTANT: Tell the service to hide the instant black mask now that our UI is ready
        lockManager.requestOverlayHide()
    }
    
    private fun attemptBiometricUnlock() {
        lifecycleScope.launch {
            // Check if biometric is enabled in settings (default true)
            // Use kotlinx.coroutines.flow.first() to get current value
            val isEnabled = settingsManager.biometricEnabled.first()
            if (isEnabled) {
                showBiometricPrompt()
            }
        }
    }
    


    private fun setupBiometric() {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        biometricPrompt = androidx.biometric.BiometricPrompt(this, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlock()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Show error or just let UI remain for PIN
                    // if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) ...
                }
            })

        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock App")
            .setSubtitle("Authenticate to access ${lockedPackageName ?: "App"}")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }
    
    private fun unlock() {
        lockedPackageName?.let {
            lockManager.onAppUnlocked(it)
        }
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Smooth exit
    }

    override fun onPause() {
        super.onPause()
        // If we leave the overlay (e.g. Home button), we want to finish so it doesn't stay in recents
        // AND to re-trigger lock next time.
        finish() 
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Go to Home screen instead of Back
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}

// End of file
