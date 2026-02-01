package com.lockeyy.di

import android.content.Context
import androidx.room.Room
import com.lockeyy.data.AppDatabase
import com.lockeyy.data.dao.LockedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lockeyy_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideLockedAppDao(db: AppDatabase): LockedAppDao {
        return db.lockedAppDao()
    }
}
