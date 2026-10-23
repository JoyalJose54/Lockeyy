package com.ssecure.applock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ssecure.applock.data.UnlockedSessionManager

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            Log.d("ScreenReceiver", "Screen turned off. Clearing unlocked sessions.")
            UnlockedSessionManager.clearSessions()
        }
    }
}
