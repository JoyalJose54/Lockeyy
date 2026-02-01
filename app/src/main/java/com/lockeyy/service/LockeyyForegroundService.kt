package com.lockeyy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lockeyy.R
import com.lockeyy.core.LockManager
import com.lockeyy.core.UsageStatsHelper
import com.lockeyy.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LockeyyForegroundService : Service() {

    @Inject
    lateinit var usageStatsHelper: UsageStatsHelper

    @Inject
    lateinit var lockManager: LockManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val CHANNEL_ID = "LockeyyForegroundServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? = null

    private var isScreenOn = true

    private val screenStateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    lockManager.clearAllSessions()
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
        
        startFallbackMonitoring()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Lockeyy Security Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lockeyy is Active")
            .setContentText("Protecting your apps in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startFallbackMonitoring() {
        serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (isScreenOn) {
                    checkForegroundApp()
                }
            }
        }
    }

    private fun checkForegroundApp() {
        val packageName = usageStatsHelper.getForegroundPackage()
        packageName?.let {
            if (lockManager.isAppLocked(it)) {
                 val intent = android.content.Intent(applicationContext, com.lockeyy.ui.lock.LockOverlayActivity::class.java).apply {
                     addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                     addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                     addFlags(android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                     putExtra("LOCKED_PACKAGE_NAME", it)
                 }
                 startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        // serviceScope.cancel() 
    }
}
