package com.ssecure.applock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ssecure.applock.MainActivity
import com.ssecure.applock.R
import com.ssecure.applock.data.AppLockPreferences
import com.ssecure.applock.data.UnlockedSessionManager
import com.ssecure.applock.ui.lock.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppLockForegroundService : Service() {
    private val tag = "AppLockForeground"
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var monitorJob: Job? = null
    private lateinit var prefs: AppLockPreferences
    private var lastForegroundApp: String? = null

    override fun onCreate() {
        super.onCreate()
        prefs = AppLockPreferences.getInstance(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.isAppLockEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return

        monitorJob = scope.launch {
            while (isActive) {
                if (prefs.isAppLockEnabled) {
                    val currentApp = getForegroundPackage(usageStatsManager)
                    if (currentApp != null && currentApp != packageName && currentApp != "com.android.systemui") {
                        val locked = prefs.getLockedPackages()
                        if (locked.contains(currentApp) && !UnlockedSessionManager.isPackageUnlocked(currentApp)) {
                            if (lastForegroundApp != currentApp) {
                                lastForegroundApp = currentApp
                                launchLockScreen(currentApp)
                            }
                        } else {
                            lastForegroundApp = null
                        }
                    }
                }
                delay(250) // High-frequency polling loop for non-accessibility devices
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager): String? {
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 2000, time)
        val event = UsageEvents.Event()
        var topPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                topPackage = event.packageName
            }
        }
        return topPackage
    }

    private fun launchLockScreen(targetPackage: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("S Secure Protection Active")
            .setContentText("Monitoring and protecting your selected apps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "S Secure Protection Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps S Secure AppLock service active in the background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "ssecure_protection_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, AppLockForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, AppLockForegroundService::class.java))
        }
    }
}
