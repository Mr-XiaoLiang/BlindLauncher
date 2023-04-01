package com.lollipop.blindlauncher

import android.app.Application
import com.lollipop.blindlauncher.db.HiddenInfoManager

class LauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        HiddenInfoManager.init(this)
    }

}