package com.lockeyy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LockeyyApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
