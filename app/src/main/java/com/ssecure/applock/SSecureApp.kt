package com.ssecure.applock

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.receiver.SamsungSystemBridge
import com.ssecure.applock.receiver.ScreenReceiver
import com.ssecure.applock.service.AppLockForegroundService

class SSecureApp : Application() {
    private val screenReceiver = ScreenReceiver()

    override fun onCreate() {
        super.onCreate()

        try {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } catch (e: Exception) {
            Log.w("LockeyyApp", "Could not register screen receiver: ${e.message}")
        }

        try {
            val prefs = AppLockPreferences.getInstance(this)
            if (prefs.isAppLockEnabled) {
                AppLockForegroundService.startService(this)
                SamsungSystemBridge.syncWithSamsungSystem(this, true, prefs.lockType.id)
            }
        } catch (e: Exception) {
            Log.w("LockeyyApp", "Initial service start deferred: ${e.message}")
        }
    }
}
