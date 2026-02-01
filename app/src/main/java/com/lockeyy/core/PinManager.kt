package com.lockeyy.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lockeyy_settings")

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStoreManager: KeyStoreManager
) {
    private val PIN_KEY = stringPreferencesKey("app_lock_pin")
    
    suspend fun savePin(pin: String) {
        try {
            val encryptedPin = keyStoreManager.encrypt(pin)
            context.dataStore.edit { preferences ->
                preferences[PIN_KEY] = encryptedPin
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error? Maybe log.
        }
    }

    suspend fun validatePin(inputPin: String): Boolean {
        val storedPinEncrypted = context.dataStore.data.map { preferences ->
            preferences[PIN_KEY]
        }.first()
        
        return if (storedPinEncrypted.isNullOrEmpty()) {
            inputPin == "1234" 
        } else {
            try {
                val decryptedPin = keyStoreManager.decrypt(storedPinEncrypted)
                decryptedPin == inputPin
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun hasPin(): Boolean {
        val storedPin = context.dataStore.data.map { preferences ->
            preferences[PIN_KEY]
        }.first()
        return !storedPin.isNullOrEmpty()
    }
}
