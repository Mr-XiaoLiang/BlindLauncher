package com.lollipop.blindlauncher.utils

import android.content.Context
import androidx.room.Room
import com.lollipop.blindlauncher.R
import com.lollipop.blindlauncher.db.AppUsage
import com.lollipop.blindlauncher.db.AppUsageDatabase
import com.lollipop.blindlauncher.doAsync
import com.lollipop.blindlauncher.onUI

class AppLaunchHelper(
    private val context: Context,
    private val listener: Listener,
    private val appInfoList: ArrayList<AppInfo> = ArrayList()
) : List<AppLaunchHelper.AppInfo> by appInfoList {

    companion object {
        const val ACTION_VOICE_OFF = "ACTION_VOICE_OFF"
    }

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
            appInfoList.add(
                appInfoList.size / 2,
                AppInfo(
                    ACTION_VOICE_OFF,
                    context.getString(R.string.voice_off),
                    0
                )
            )
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
                listener.onAppListSortChanged()
            }
        }
    }

    fun launch(info: AppInfo?): LaunchResult {
        info ?: return LaunchResult.APP_NOT_FOUND
        return when (info.pkgName) {
            ACTION_VOICE_OFF -> {
                listener.callVoiceOff()
                LaunchResult.SUCCESS
            }
            else -> {
                launchApp(info)
            }
        }
    }

    private fun launchApp(info: AppInfo): LaunchResult {
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

    interface Listener {
        fun onAppListSortChanged()
        fun callVoiceOff()
    }

}