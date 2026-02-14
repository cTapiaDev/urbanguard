package com.example.urbanguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.urbanguard.data.local.dao.ReportDao
import com.example.urbanguard.data.local.entity.ReportEntity

@Database(entities = [ReportEntity::class], version = 1, exportSchema = false)
abstract class UrbanGuardDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}