package com.lockeyy.core

import com.lockeyy.data.dao.LockedAppDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor(
    private val lockedAppDao: LockedAppDao,
    private val settingsManager: SettingsManager
) {
    // Cache of packages that are configured to be locked
    private val lockedPackages = Collections.synchronizedSet(HashSet<String>())
    
    // Cache of packages that are currently "unlocked" temporarily (session)
    // Map<PackageName, Timestamp>
    private val validSessions = ConcurrentHashMap<String, Long>()

    // 0L = Immediately (on exit), -1L = Screen Off, >0L = Milliseconds
    private var currentRelockTimeout: Long = 0L
    
    fun initialize(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            lockedAppDao.getAllLockedApps().collect { list ->
                lockedPackages.clear()
                list.forEach { lockedPackages.add(it.packageName) }
            }
        }
        scope.launch(Dispatchers.IO) {
            settingsManager.relockTimeout.collect { 
                currentRelockTimeout = it 
            }
        }
    }

    fun onAppForegrounded(currentPackage: String) {
        // If "Immediately" (0L) is selected, we clear sessions for other apps when switching.
        // Exception: Don't clear if switching to/from our own app (Lock Screen/Settings)
        if (currentPackage == "com.lockeyy") return 

        if (currentRelockTimeout == 0L) {
             val iterator = validSessions.keys().asIterator()
             while (iterator.hasNext()) {
                 val pkg = iterator.next()
                 if (pkg != currentPackage) {
                     validSessions.remove(pkg)
                 }
             }
        }
    }

    fun isAppLocked(packageName: String): Boolean {
        // 1. Is it in the protected list?
        if (!lockedPackages.contains(packageName)) return false
        
        // 2. Do we have a valid session?
        if (hasValidSession(packageName)) return false
        
        return true
    }

    private fun hasValidSession(packageName: String): Boolean {
        val timestamp = validSessions[packageName] ?: return false
        
        if (currentRelockTimeout == -1L) return true // Valid until screen off (cleared externally)
        if (currentRelockTimeout == 0L) return true // Valid until cleared by onAppForegrounded
        
        // Time based validity
        val elapsed = System.currentTimeMillis() - timestamp
        return elapsed < currentRelockTimeout
    }

    private var unlockListener: ((String) -> Unit)? = null
    private var overlayListener: (() -> Unit)? = null

    fun setUnlockListener(listener: (String) -> Unit) {
        this.unlockListener = listener
    }
    
    fun setOverlayHideListener(listener: () -> Unit) {
        this.overlayListener = listener
    }

    fun onAppUnlocked(packageName: String) {
        validSessions[packageName] = System.currentTimeMillis()
        unlockListener?.invoke(packageName)
        overlayListener?.invoke() // Also hide overlay on unlock (redundant but safe)
    }
    
    fun requestOverlayHide() {
        overlayListener?.invoke()
    }
    
    fun clearSession(packageName: String) {
        validSessions.remove(packageName)
    }

    fun clearAllSessions() {
        validSessions.clear()
    }
}
