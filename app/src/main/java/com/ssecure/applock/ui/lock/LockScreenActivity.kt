package com.ssecure.applock.ui.lock

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.data.UnlockedSessionManager
import com.ssecure.applock.model.LockType
import com.ssecure.applock.service.AppLockAccessibilityService
import kotlinx.coroutines.delay

class LockScreenActivity : FragmentActivity() {
    private lateinit var prefs: AppLockPreferences
    private var targetPackage: String? = null
    private var appName: String = "Application"
    private var appIconDrawable: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Zero-delay window transition
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)

        // Block screenshots / task switcher previews for security
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        prefs = AppLockPreferences.getInstance(this)
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)

        loadTargetAppDetails()

        // Handle Back button to securely go Home instead of exposing the locked app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelAndGoHome()
            }
        })

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LockScreenContent(
                        appName = appName,
                        appIcon = appIconDrawable,
                        lockType = prefs.lockType,
                        isBiometricsAvailable = prefs.isBiometricsEnabled && canAuthenticateBiometrics(),
                        onVerifyPin = { pin ->
                            if (prefs.verifyPin(pin)) {
                                onUnlockSuccess()
                                true
                            } else {
                                vibrateError()
                                false
                            }
                        },
                        onVerifyPattern = { pattern ->
                            if (prefs.verifyPattern(pattern)) {
                                onUnlockSuccess()
                                true
                            } else {
                                vibrateError()
                                false
                            }
                        },
                        onRequestBiometric = {
                            showBiometricPrompt()
                        },
                        onCancelClick = {
                            cancelAndGoHome()
                        }
                    )
                }
            }
        }

        // Dismiss the background accessibility shield once our activity UI has drawn
        window.decorView.post {
            AppLockAccessibilityService.dismissShield()
        }

        // Auto trigger biometric prompt if enabled
        if (prefs.isBiometricsEnabled && canAuthenticateBiometrics()) {
            showBiometricPrompt()
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
        AppLockAccessibilityService.dismissShield()
    }

    override fun onDestroy() {
        AppLockAccessibilityService.dismissShield()
        super.onDestroy()
    }

    private fun cancelAndGoHome() {
        AppLockAccessibilityService.sendGoHome(this)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun loadTargetAppDetails() {
        val pkg = targetPackage ?: return
        try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            appName = pm.getApplicationLabel(appInfo).toString()
            appIconDrawable = pm.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            appName = "Application"
        }
    }

    private fun onUnlockSuccess() {
        targetPackage?.let {
            UnlockedSessionManager.unlockPackage(it)
        }
        AppLockAccessibilityService.dismissShield()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun canAuthenticateBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onUnlockSuccess()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock $appName")
            .setSubtitle("Use your fingerprint to continue")
            .setNegativeButtonText("Use ${prefs.lockType.title}")
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun vibrateError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(150)
        }
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }
}

@Composable
fun LockScreenContent(
    appName: String,
    appIcon: Drawable?,
    lockType: LockType,
    isBiometricsAvailable: Boolean,
    onVerifyPin: (String) -> Boolean,
    onVerifyPattern: (String) -> Boolean,
    onRequestBiometric: () -> Unit,
    onCancelClick: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isError) {
        if (isError) {
            delay(1000)
            isError = false
            pinInput = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header - brought down comfortably below status bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    val bitmap = remember(appIcon) {
                        try {
                            appIcon.toBitmap(152, 152)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = appName,
                            modifier = Modifier.size(56.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appName,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isError) {
                    "Incorrect credentials. Try again."
                } else {
                    when (lockType) {
                        LockType.PATTERN -> "Draw your pattern to unlock"
                        LockType.PIN, LockType.PASSWORD -> "Enter your PIN to unlock"
                    }
                },
                fontSize = 14.sp,
                color = if (isError) Color(0xFFEF4444) else Color(0xFF94A3B8)
            )
        }

        // Pushes pattern/PIN directly to the bottom of the screen
        Spacer(modifier = Modifier.weight(1f))

        // Lock Method input - sits at the bottom of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            when (lockType) {
                LockType.PATTERN -> {
                    PatternLockView(
                        isError = isError,
                        onPatternCompleted = { pattern ->
                            val success = onVerifyPattern(pattern)
                            if (!success) {
                                isError = true
                                attemptCount++
                            }
                        },
                        onCancelClick = onCancelClick
                    )
                }
                LockType.PIN, LockType.PASSWORD -> {
                    PinLockView(
                        pinLength = pinInput.length,
                        isError = isError,
                        onDigitClick = { digit ->
                            if (pinInput.length < 4 && !isError) {
                                val newPin = pinInput + digit
                                pinInput = newPin
                                if (newPin.length == 4) {
                                    val success = onVerifyPin(newPin)
                                    if (!success) {
                                        isError = true
                                        attemptCount++
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (pinInput.isNotEmpty() && !isError) {
                                pinInput = pinInput.dropLast(1)
                            }
                        },
                        onBiometricClick = if (isBiometricsAvailable) onRequestBiometric else null,
                        onCancelClick = onCancelClick
                    )
                }
            }
        }
    }
}
