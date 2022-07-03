package com.lollipop.blindlauncher.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appUsage")
class AppUsage(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "package_name", defaultValue = "")
    val packageName: String,
    @ColumnInfo(name = "launch_count", defaultValue = "0")
    var launchCount: Int
)