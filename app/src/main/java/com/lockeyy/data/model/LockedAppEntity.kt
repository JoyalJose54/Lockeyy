package com.lockeyy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val isLocked: Boolean = true,
    val lockedAt: Long = System.currentTimeMillis()
)
