package com.lockeyy.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.lockeyy.ui.lock.LockOverlayActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.lockeyy.core.LockManager
import kotlinx.coroutines.launch

class LockeyyAccessibilityService : AccessibilityService() {

    private val TAG = "LockeyyService"

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LockeyyAccessibilityServiceEntryPoint {
        fun getLockManager(): LockManager
        fun getOverlayManager(): com.lockeyy.ui.overlay.BlockingOverlayManager
    }

    private lateinit var lockManager: LockManager
    private lateinit var overlayManager: com.lockeyy.ui.overlay.BlockingOverlayManager
    
    // We need a scope for flow collection updates in LockManager, 
    // but LockManager handling it internally with passed scope is okay or we launch here.
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service Connected")
        
        // Manual Hilt Injection
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            LockeyyAccessibilityServiceEntryPoint::class.java
        )
        lockManager = entryPoint.getLockManager()
        overlayManager = entryPoint.getOverlayManager()
        
        // Initialize the cache
        lockManager.initialize(serviceScope)
        
        // Listen for unlock events to hide the mask
        lockManager.setUnlockListener {
            // Ensure we run on main thread if not already
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                overlayManager.hideMask()
            }
        }
        
        // Listen for generic hide requests (from Activity onResume)
        lockManager.setOverlayHideListener {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                overlayManager.hideMask()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore self
            if (packageName == applicationContext.packageName) return 
            
            // Ignore if already showing (simple check, or manager handles it)

            lockManager.onAppForegrounded(packageName)
            if (lockManager.isAppLocked(packageName)) {
                Log.d(TAG, "Locking: $packageName")
                // Show instant black mask BEFORE starting activity
                overlayManager.showMask()
                showLockScreen(packageName)
            }
        }
    }

    private fun showLockScreen(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("LOCKED_PACKAGE_NAME", packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service Interrupted")
        overlayManager.hideMask()
        // serviceScope.cancel() // Optional cleanup
    }
}
