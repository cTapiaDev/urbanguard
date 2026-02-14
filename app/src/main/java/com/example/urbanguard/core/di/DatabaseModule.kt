package com.example.urbanguard.core.di

import android.content.Context
import androidx.room.Room
import com.example.urbanguard.data.local.UrbanGuardDatabase
import com.example.urbanguard.data.local.dao.ReportDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UrbanGuardDatabase {
        return Room.databaseBuilder(
            context,
            UrbanGuardDatabase::class.java,
            "urbanguard_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideReportDao(database: UrbanGuardDatabase): ReportDao {
        return database.reportDao()
    }
}