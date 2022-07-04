package com.lollipop.blindlauncher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * APP列表的原始数据集合对象
 * 它是静态的，因为希望持续的保存数据，并且数据变化并不大
 */
object AppListHelper : List<AppListHelper.AppResolveInfo> {

    /**
     * 应用原始信息
     */
    private val appResolveInfo = ArrayList<AppResolveInfo>()

    private var needReloadAppInfo = true

    /**
     * 注册应用安装包变化的监听器
     * 主要用于软件服务长时间运行的情况下，需要更新包信息的场景
     */
    fun registerPackageChangeReceiver(context: Context): BroadcastReceiver {
        return AppChangedBroadcastReceiver().apply {
            init(context)
        }
    }

    /**
     * 加载APP的信息
     */
    fun loadAppInfo(context: Context) {
        synchronized(this) {
            if (appResolveInfo.isEmpty() || needReloadAppInfo) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                val appList = pm.queryIntentActivities(mainIntent, 0)
                appResolveInfo.clear()
                for (info in appList) {
                    appResolveInfo.add(AppResolveInfo(info))
                }
                needReloadAppInfo = false
            }
        }
    }

    class AppResolveInfo(
        val resolveInfo: ResolveInfo
    ) {

        private var label: CharSequence = ""

        val pkgName: String = resolveInfo.activityInfo.packageName

        fun getLabel(context: Context): CharSequence {
            return getLabel(context.packageManager)
        }

        fun getLabel(packageManager: PackageManager): CharSequence {
            if (label.isEmpty()) {
                val newLabel = resolveInfo.loadLabel(packageManager)
                label = newLabel
            }
            return label
        }

    }

    private class AppChangedBroadcastReceiver : BroadcastReceiver(), LifecycleEventObserver {

        private var contentReference: WeakReference<Context>? = null

        fun init(context: Context) {
            contentReference = WeakReference(context)
            context.registerReceiver(
                this,
                IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addAction(Intent.ACTION_PACKAGE_CHANGED)
                }
            )
            if (context is LifecycleOwner) {
                context.lifecycle.addObserver(this)
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            needReloadAppInfo = true
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState == Lifecycle.State.DESTROYED) {
                contentReference?.get()?.unregisterReceiver(this)
                contentReference = null
            }
        }
    }

    override val size: Int
        get() {
            return appResolveInfo.size
        }

    override fun contains(element: AppResolveInfo): Boolean {
        return appResolveInfo.contains(element)
    }

    override fun containsAll(elements: Collection<AppResolveInfo>): Boolean {
        return appResolveInfo.containsAll(elements)
    }

    override fun get(index: Int): AppResolveInfo {
        return appResolveInfo[index]
    }

    override fun indexOf(element: AppResolveInfo): Int {
        return appResolveInfo.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return appResolveInfo.isEmpty()
    }

    override fun iterator(): Iterator<AppResolveInfo> {
        return appResolveInfo.iterator()
    }

    override fun lastIndexOf(element: AppResolveInfo): Int {
        return appResolveInfo.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<AppResolveInfo> {
        return appResolveInfo.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<AppResolveInfo> {
        return appResolveInfo.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<AppResolveInfo> {
        return appResolveInfo.subList(fromIndex, toIndex)
    }

}