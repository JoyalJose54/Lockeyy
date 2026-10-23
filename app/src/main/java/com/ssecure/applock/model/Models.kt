package com.ssecure.applock.model

enum class LockType(val id: Int, val title: String) {
    PATTERN(1, "Pattern"),
    PIN(2, "PIN"),
    PASSWORD(3, "Password");

    companion object {
        fun fromId(id: Int): LockType = entries.find { it.id == id } ?: PIN
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isLocked: Boolean = false,
    val isMasked: Boolean = false
)
