package com.ssecure.applock

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.receiver.SamsungSystemBridge
import com.ssecure.applock.receiver.ScreenReceiver
import com.ssecure.applock.service.AppLockForegroundService

class SSecureApp : Application() {
    private val screenReceiver = ScreenReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)

        val prefs = AppLockPreferences.getInstance(this)
        if (prefs.isAppLockEnabled) {
            AppLockForegroundService.startService(this)
            SamsungSystemBridge.syncWithSamsungSystem(this, true, prefs.lockType.id)
        }
    }
}
