package com.lollipop.blindlauncher.utils

import android.content.Context
import androidx.room.Room
import com.lollipop.blindlauncher.db.AppUsage
import com.lollipop.blindlauncher.db.AppUsageDatabase
import com.lollipop.blindlauncher.doAsync
import com.lollipop.blindlauncher.onUI

class AppLaunchHelper(
    private val context: Context,
    private val onAppListSortChangedListener: OnAppListSortChangedListener,
    private val appInfoList: ArrayList<AppInfo> = ArrayList()
) : List<AppLaunchHelper.AppInfo> by appInfoList {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppUsageDatabase::class.java, "app_usage_database"
    ).build()

    fun init() {
        AppListHelper.registerPackageChangeReceiver(context)
    }

    fun loadData() {
        AppListHelper.loadAppInfo(context)
        val tempList = ArrayList<AppInfo>()
        AppListHelper.forEach {
            tempList.add(
                AppInfo(
                    it.pkgName,
                    it.getLabel(context).toString(),
                    0
                )
            )
        }
        synchronized(appInfoList) {
            appInfoList.clear()
            appInfoList.addAll(tempList)
        }
        doAsync {
            val allUsage = db.usageDao().getAll()
            appInfoList.forEach {
                it.launchCount = findLaunchCount(allUsage, it.pkgName)
            }
            synchronized(appInfoList) {
                appInfoList.sortBy { it.launchCount }
            }
            onUI {
                onAppListSortChangedListener.onAppListSortChanged()
            }
        }
    }

    fun launch(info: AppInfo?): LaunchResult {
        info ?: return LaunchResult.APP_NOT_FOUND
        info.launchCount++
        doAsync {
            db.usageDao().updateOrInsert(info.pkgName, info.launchCount)
        }
        val intent = context.packageManager.getLaunchIntentForPackage(info.pkgName)
            ?: return LaunchResult.APP_NOT_FOUND
        try {
            context.startActivity(intent)
        } catch (e: Throwable) {
            e.printStackTrace()
            return LaunchResult.ERROR
        }
        return LaunchResult.SUCCESS
    }

    private fun findLaunchCount(list: List<AppUsage>, pkgName: String): Int {
        return list.find { it.packageName == pkgName }?.launchCount ?: 0
    }

    class AppInfo(
        val pkgName: String,
        val label: String,
        var launchCount: Int
    )

    enum class LaunchResult {
        SUCCESS,
        APP_NOT_FOUND,
        ERROR
    }

    fun interface OnAppListSortChangedListener {
        fun onAppListSortChanged()
    }

}