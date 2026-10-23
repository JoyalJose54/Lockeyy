package com.ssecure.applock.service

import android.accessibilityservice.AccessibilityService
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ssecure.applock.R
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.data.UnlockedSessionManager
import com.ssecure.applock.ui.lock.LockScreenActivity

class AppLockAccessibilityService : AccessibilityService() {
    private val tag = "AppLockAccessibility"
    private lateinit var prefs: AppLockPreferences
    private lateinit var windowManager: WindowManager
    private var instantShieldView: View? = null
    private var currentShieldPackage: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // 1-second fail-safe watchdog timer: prevents frozen or stuck black screens under any circumstance
    private val watchdogRunnable = Runnable {
        Log.w(tag, "Fail-safe watchdog triggered: auto-dismissing shield and returning home")
        dismissInstantShield()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = AppLockPreferences.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        instance = this
        Log.d(tag, "S Secure Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_VIEW_CLICKED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own package and system UI/launcher dialogs
        if (packageName == applicationContext.packageName ||
            packageName == "com.android.systemui" ||
            packageName == "android"
        ) {
            return
        }

        if (!prefs.isAppLockEnabled) return

        val lockedPackages = prefs.getLockedPackages()
        if (lockedPackages.contains(packageName)) {
            // Check if user already unlocked this package during current session
            if (!UnlockedSessionManager.isPackageUnlocked(packageName)) {
                Log.d(tag, "Intercepting locked package: $packageName")
                // Only attach overlay if device has granted overlay permission
                if (Settings.canDrawOverlays(this)) {
                    showInstantShield(packageName)
                }
                launchLockScreen(packageName)
            }
        }
    }

    private fun showInstantShield(packageName: String) {
        if (instantShieldView != null) return

        try {
            currentShieldPackage = packageName

            // Root container with touch listener for emergency escape
            val root = FrameLayout(this).apply {
                setBackgroundColor(0xFF000000.toInt())
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    // Emergency escape: tapping anywhere immediately closes shield and goes home
                    dismissInstantShield()
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }

            // Central S Secure Branding
            val centerCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            val icon = ImageView(this).apply {
                setImageResource(R.drawable.ic_lock)
            }
            centerCol.addView(icon, LinearLayout.LayoutParams(110, 110))

            val label = TextView(this).apply {
                text = "S Secure"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 18f
                setPadding(0, 24, 0, 0)
            }
            centerCol.addView(label)

            val subLabel = TextView(this).apply {
                text = "Securing application…"
                setTextColor(0xFF94A3B8.toInt())
                textSize = 13f
                setPadding(0, 8, 0, 0)
            }
            centerCol.addView(subLabel)

            root.addView(
                centerCol,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )

            // Cancel button at bottom
            val cancelBtn = TextView(this).apply {
                text = "Cancel"
                setTextColor(0xFF38BDF8.toInt())
                textSize = 15f
                setPadding(40, 24, 40, 48)
                setOnClickListener {
                    dismissInstantShield()
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
            root.addView(
                cancelBtn,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                )
            )

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            windowManager.addView(root, params)
            instantShieldView = root

            // Fail-safe watchdog: auto-dismiss within 1000ms if LockScreenActivity fails or takes too long
            mainHandler.removeCallbacks(watchdogRunnable)
            mainHandler.postDelayed(watchdogRunnable, 1000)

            Log.d(tag, "Instant fail-safe shield attached for $packageName")
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach instant shield: ${e.message}")
            dismissInstantShield()
        }
    }

    fun dismissInstantShield() {
        mainHandler.removeCallbacks(watchdogRunnable)
        val view = instantShieldView ?: return
        instantShieldView = null
        currentShieldPackage = null

        try {
            windowManager.removeViewImmediate(view)
            Log.d(tag, "Instant shield removed immediately")
        } catch (e: Exception) {
            try {
                windowManager.removeView(view)
            } catch (ignored: Exception) {}
        }
    }

    private fun launchLockScreen(targetPackage: String) {
        try {
            val intent = Intent(this, LockScreenActivity::class.java).apply {
                putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, targetPackage)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch LockScreenActivity: ${e.message}")
            dismissInstantShield()
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(watchdogRunnable)
        dismissInstantShield()
        instance = null
        super.onDestroy()
    }

    override fun onInterrupt() {
        mainHandler.removeCallbacks(watchdogRunnable)
        dismissInstantShield()
        Log.d(tag, "S Secure Accessibility Service Interrupted")
    }

    companion object {
        private var instance: AppLockAccessibilityService? = null

        fun dismissShield() {
            instance?.dismissInstantShield()
        }

        fun sendGoHome(context: Context) {
            instance?.let { service ->
                service.performGlobalAction(GLOBAL_ACTION_HOME)
                service.dismissInstantShield()
            } ?: run {
                val home = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(home)
            }
        }
    }
}
