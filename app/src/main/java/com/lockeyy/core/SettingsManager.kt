package com.lockeyy.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val RELOCK_TIMEOUT_KEY = androidx.datastore.preferences.core.longPreferencesKey("relock_timeout")

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED_KEY] ?: true // Default true
    }
    
    val relockTimeout: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[RELOCK_TIMEOUT_KEY] ?: 0L // Default 0 (Immediately)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED_KEY] = enabled }
    }

    suspend fun setRelockTimeout(timeout: Long) {
        context.dataStore.edit { it[RELOCK_TIMEOUT_KEY] = timeout }
    }
}
