package com.lollipop.blindlauncher.utils

import android.content.Context
import androidx.room.Room
import com.lollipop.blindlauncher.R
import com.lollipop.blindlauncher.db.AppUsage
import com.lollipop.blindlauncher.db.AppUsageDatabase
import com.lollipop.blindlauncher.doAsync
import com.lollipop.blindlauncher.onUI

/**
 * APP选择器
 */
class AppLaunchHelper(
    private val context: Context,
    private val listener: Listener,
    private val appInfoList: ArrayList<AppInfo> = ArrayList()
) : List<AppLaunchHelper.AppInfo> by appInfoList {

    companion object {
        const val ACTION_VOICE_OFF = "ACTION_VOICE_OFF"
    }

    /**
     * 已选中的Index
     */
    private var selectedIndex = -1

    /**
     * 已选中的APP
     */
    private var selectedApp: AppInfo? = null

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppUsageDatabase::class.java, "app_usage_database"
    ).build()

    /**
     * 初始化方法用于注册系统的应用安装与卸载事件
     */
    fun init() {
        AppListHelper.registerPackageChangeReceiver(context)
    }

    /**
     * 加载数据，它会加载并更新app的列表并且按照使用频率排序
     */
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
                appInfoList.sortBy { 0 - it.launchCount }
            }
            selectedIndex = -1
            selectedApp = null
            onUI {
                listener.onAppListSortChanged()
            }
        }
    }

    /**
     * 启动当前选中的APP
     */
    fun launch(): LaunchResult {
        val info = selectedApp
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

    private fun selectedApp(index: Int) {
        if (selectedIndex != index) {
            selectedIndex = index
            selectedApp = if (index in 0 until size) {
                val appInfo = this[index]
                appInfo
            } else {
                null
            }
            listener.onAppSelected(selectedApp)
        }
    }

    /**
     * 选中上一个
     */
    fun selectLast() {
        onPartitionsChange(selectedIndex - 1)
    }

    /**
     * 选中下一个
     */
    fun selectNext() {
        onPartitionsChange(selectedIndex + 1)
    }

    private fun onPartitionsChange(index: Int) {
        var newIndex = index
        if (isEmpty()) {
            newIndex = -1
        } else {
            if (newIndex < 0) {
                newIndex = size - 1
            }
            if (newIndex > size - 1) {
                newIndex = 0
            }
        }
        selectedApp(newIndex)
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
        fun onAppSelected(info: AppInfo?)
        fun callVoiceOff()
    }

}