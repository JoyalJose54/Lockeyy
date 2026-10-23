package com.ssecure.applock.receiver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log

object SamsungSystemBridge {
    private const val TAG = "SamsungSystemBridge"
    const val ACTION_APP_LOCK_ENABLE = "com.samsung.applock.intent.action.APPLOCK_ENABLE_CHANGED"
    const val PERMISSION_APP_LOCK_STATUS_CHANGED = "com.samsung.applock.permission.STATUSCHANGED"
    const val APP_LOCK_ENABLED_KEY = "app_lock_enabled"
    const val APP_LOCK_LOCK_TYPE_KEY = "applock_lock_type"

    fun syncWithSamsungSystem(context: Context, isEnabled: Boolean, lockTypeId: Int) {
        try {
            // Attempt writing to Settings.Secure (successful if granted WRITE_SECURE_SETTINGS via ADB/system)
            Settings.Secure.putInt(context.contentResolver, APP_LOCK_ENABLED_KEY, if (isEnabled) 1 else 0)
            Settings.Secure.putInt(context.contentResolver, APP_LOCK_LOCK_TYPE_KEY, lockTypeId)
            Log.d(TAG, "Successfully synced app_lock_enabled ($isEnabled) with Settings.Secure")
        } catch (e: SecurityException) {
            Log.d(TAG, "WRITE_SECURE_SETTINGS not granted, running in standard mode.")
        }

        try {
            // Broadcast exact Samsung S Secure / Launcher intent discovered during REA analysis
            val intent = Intent(ACTION_APP_LOCK_ENABLE).apply {
                val bundle = Bundle().apply {
                    putBoolean("android.intent.extra.SUBJECT", isEnabled)
                }
                putExtras(bundle)
            }
            context.sendBroadcast(intent, PERMISSION_APP_LOCK_STATUS_CHANGED)
            Log.d(TAG, "Broadcasted APPLOCK_ENABLE_CHANGED ($isEnabled) to system launcher")
        } catch (e: Exception) {
            Log.d(TAG, "Broadcast failed (non-Samsung device or permission absent): ${e.message}")
        }
    }
}
