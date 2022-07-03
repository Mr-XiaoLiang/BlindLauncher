package com.lollipop.blindlauncher.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppUsage::class], version = 1, exportSchema = false)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun usageDao(): AppUsageDao
}