package com.ssecure.applock.data

import android.content.Context
import android.content.SharedPreferences
import com.ssecure.applock.model.LockType
import java.security.MessageDigest

class AppLockPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var lockType: LockType
        get() = LockType.fromId(prefs.getInt(KEY_LOCK_TYPE, LockType.PIN.id))
        set(value) = prefs.edit().putInt(KEY_LOCK_TYPE, value.id).apply()

    var isBiometricsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRICS_ENABLED, value).apply()

    fun getLockedPackages(): Set<String> {
        return prefs.getStringSet(KEY_LOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setLockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_LOCKED_PACKAGES, packages).apply()
    }

    fun toggleAppLocked(packageName: String): Boolean {
        val current = getLockedPackages().toMutableSet()
        val isNowLocked = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        setLockedPackages(current)
        return isNowLocked
    }

    fun getMaskedPackages(): Set<String> {
        return prefs.getStringSet(KEY_MASKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setMaskedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_MASKED_PACKAGES, packages).apply()
    }

    fun toggleAppMasked(packageName: String): Boolean {
        val current = getMaskedPackages().toMutableSet()
        val isNowMasked = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        setMaskedPackages(current)
        return isNowMasked
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hashString(pin)).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return pin == "1234" // Default PIN
        return storedHash == hashString(pin)
    }

    fun setPattern(pattern: String) {
        prefs.edit().putString(KEY_PATTERN_HASH, hashString(pattern)).apply()
    }

    fun verifyPattern(pattern: String): Boolean {
        val storedHash = prefs.getString(KEY_PATTERN_HASH, null) ?: return pattern == "012" // Default 3 dots
        return storedHash == hashString(pattern)
    }

    fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD_HASH, hashString(password)).apply()
    }

    fun verifyPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return storedHash == hashString(password)
    }

    fun hasSetPin(): Boolean = prefs.contains(KEY_PIN_HASH)
    fun hasSetPattern(): Boolean = prefs.contains(KEY_PATTERN_HASH)
    fun hasSetPassword(): Boolean = prefs.contains(KEY_PASSWORD_HASH)

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "s_secure_preferences"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_LOCK_TYPE = "applock_lock_type"
        private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
        private const val KEY_LOCKED_PACKAGES = "locked_packages"
        private const val KEY_MASKED_PACKAGES = "masked_packages"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PATTERN_HASH = "pattern_hash"
        private const val KEY_PASSWORD_HASH = "password_hash"

        @Volatile
        private var instance: AppLockPreferences? = null

        fun getInstance(context: Context): AppLockPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppLockPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
