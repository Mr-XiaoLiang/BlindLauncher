package com.lollipop.blindlauncher.db

import androidx.room.*

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM appUsage")
    fun getAll(): List<AppUsage>

    @Query("SELECT * FROM appUsage WHERE package_name = :pkg LIMIT 1")
    fun findByPkg(pkg: String): AppUsage?

    @Insert
    fun insertAll(vararg usages: AppUsage)

    @Delete
    fun delete(usage: AppUsage)

    @Update
    fun update(usage: AppUsage)

    fun updateOrInsert(pkg: String, count: Int) {
        val oldInfo = findByPkg(pkg)
        if (oldInfo != null) {
            oldInfo.launchCount = count
            update(oldInfo)
        } else {
            insertAll(AppUsage(pkg, count))
        }
    }
}