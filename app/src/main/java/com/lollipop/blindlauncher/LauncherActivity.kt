package com.lollipop.blindlauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.blindlauncher.databinding.ActivityLauncherBinding
import com.lollipop.blindlauncher.utils.*
import com.lollipop.blindlauncher.view.RotaryView

class LauncherActivity : AppCompatActivity(),
    TtsHelper.OnInitListener,
    AppLaunchHelper.OnAppListSortChangedListener,
    RotaryView.GestureListener {

    private val binding: ActivityLauncherBinding by lazyBind()

    private val ttsHelper by lazy {
        TtsHelper(this, this)
    }

    private val vibrateHelper by lazy {
        VibrateHelper(this)
    }

    private val appLaunchHelper by lazy {
        AppLaunchHelper(this, this)
    }

    private var selectedIndex = -1
    private var selectedApp: AppLaunchHelper.AppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)

        // 初始化
        ttsHelper
        appLaunchHelper.init()

        // 注册手势监听器
        binding.rotaryView.setGestureListener(this)
    }

    override fun onResume() {
        super.onResume()
        // 再次加载更新数据
        appLaunchHelper.loadData()
        // 震动表示就绪
        vibrateHelper.startUp(VibrateHelper.Vibrate.SELECTED)
    }

    private fun selectedApp(index: Int) {
        if (selectedIndex != index) {
            selectedIndex = index
            val label: String
            if (index >= 0 && index < appLaunchHelper.size) {
                val appInfo = appLaunchHelper[index]
                label = appInfo.label
                selectedApp = appInfo
            } else {
                label = getString(R.string.app_selected_none)
                selectedApp = null
            }
            setText(label)
            ttsHelper.say(label)
            vibrateHelper.startUp(VibrateHelper.Vibrate.SELECTED)
        }
    }

    private fun setText(value: CharSequence) {
        binding.textView.text = value
    }

    private fun setText(value: Int) {
        setText(getString(value))
    }

    override fun onTtsInitError() {
        setText(R.string.tts_init_error)
    }

    override fun onTtsChineseNotSupported() {
        setText(R.string.tts_not_support_chinese)
    }

    override fun onTtsReady() {
        ttsHelper.say(R.string.tts_ready)
        if (appLaunchHelper.isNotEmpty()) {
            ttsHelper.say(R.string.app_list_ready, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.destroy()
    }

    override fun onAppListSortChanged() {
        selectedIndex = -1
        selectedApp = null
        setText("")
    }

    override fun onTouchDown() {
        vibrateHelper.startUp(VibrateHelper.Vibrate.TOUCH_DOWN)
    }

    override fun onTouchUp() {
        vibrateHelper.startUp(VibrateHelper.Vibrate.TOUCH_UP)
    }

    override fun onPartitionsChange(clockwise: Boolean) {
        var newIndex = if (clockwise) {
            selectedIndex + 1
        } else {
            selectedIndex - 1
        }
        if (appLaunchHelper.isEmpty()) {
            newIndex = -1
        } else {
            if (newIndex < 0) {
                newIndex = appLaunchHelper.size - 1
            }
            if (newIndex > appLaunchHelper.size - 1) {
                newIndex = 0
            }
        }
        selectedApp(newIndex)
    }

    override fun onDoubleTap() {
        appLaunchHelper.launch(selectedApp)
    }

}