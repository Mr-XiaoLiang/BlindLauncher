package com.lollipop.blindlauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.blindlauncher.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity(), TtsHelper.OnInitListener {

    private val binding: ActivityLauncherBinding by lazyBind()

    private val ttsHelper by lazy {
        TtsHelper(this, this)
    }

    private val vibrateHelper by lazy {
        VibrateHelper(this)
    }

    private var selectedIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)

        AppListHelper.registerPackageChangeReceiver(this)

        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)

        // 初始化
        ttsHelper
    }

    override fun onResume() {
        super.onResume()
        // 再次加载更新数据
        AppListHelper.loadAppInfo(this)
        // 震动表示就绪
        vibrateHelper.startUp(VibrateHelper.Vibrate.SELECTED)

        binding.root.postDelayed({selectedApp(1)}, 3000)
    }

    private fun selectedApp(index: Int) {
        if (selectedIndex != index) {
            selectedIndex = index
            if (index >= 0 && index < AppListHelper.size) {
                val label = AppListHelper[index].getLabel(this)
                setText(label)
                ttsHelper.say(label.toString())
                vibrateHelper.startUp(VibrateHelper.Vibrate.SELECTED)
            }
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
        if (AppListHelper.isNotEmpty()) {
            ttsHelper.say(R.string.app_list_ready)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.destroy()
    }

}