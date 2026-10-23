package com.ssecure.applock.data

import android.util.Log

object UnlockedSessionManager {
    private const val TAG = "UnlockedSessionManager"
    private val unlockedPackages = mutableSetOf<String>()

    fun unlockPackage(packageName: String) {
        synchronized(unlockedPackages) {
            unlockedPackages.add(packageName)
            Log.d(TAG, "Package unlocked for current session: $packageName")
        }
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        synchronized(unlockedPackages) {
            return unlockedPackages.contains(packageName)
        }
    }

    fun clearSessions() {
        synchronized(unlockedPackages) {
            unlockedPackages.clear()
            Log.d(TAG, "All unlocked sessions cleared (e.g. screen off)")
        }
    }
}
