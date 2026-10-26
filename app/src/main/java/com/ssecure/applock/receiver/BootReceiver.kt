package com.ssecure.applock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.service.AppLockForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = AppLockPreferences.getInstance(context)
            if (prefs.isAppLockEnabled) {
                Log.d("BootReceiver", "Boot completed: Lockeyy is enabled, starting service.")
                AppLockForegroundService.startService(context)
            }
        }
    }
}
