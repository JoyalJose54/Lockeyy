package com.lockeyy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lockeyy.data.dao.LockedAppDao
import com.lockeyy.data.model.LockedAppEntity

@Database(entities = [LockedAppEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
}
