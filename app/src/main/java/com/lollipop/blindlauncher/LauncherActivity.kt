package com.lollipop.blindlauncher

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.blindlauncher.databinding.ActivityLauncherBinding
import com.lollipop.blindlauncher.utils.*
import com.lollipop.blindlauncher.view.RotaryView

/**
 * 启动器的Activity
 * 为了简化操作和引起不必要的烦恼
 * 有且只能有一个Activity，一个页面
 */
class LauncherActivity : AppCompatActivity(),
    TtsHelper.OnInitListener,
    AppLaunchHelper.Listener,
    RotaryView.GestureListener {

    /**
     * View的Binding
     */
    private val binding: ActivityLauncherBinding by lazyBind()

    /**
     * TTS语音输出工具
     */
    private val ttsHelper by lazy {
        TtsHelper(this, this)
    }

    /**
     * 震动辅助工具
     */
    private val vibrateHelper by lazy {
        VibrateHelper(this)
    }

    /**
     * APP启动工具
     */
    private val appLaunchHelper by lazy {
        AppLaunchHelper(this, this)
    }

    /**
     * 偏好设置工具
     */
    private val settings by lazy {
        LSettings(this)
    }

    /**
     * 音量开启的按键监听器
     */
    private val volumeUpTapHelper = MultipleTapHelper(invokeCount = 5, callback = ::openVoice)

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

    /**
     * 实体按钮的按下事件
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                // 监听音量点击事件来开启语音提示
                volumeUpTapHelper.onTap()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 设置显示的内容
     */
    private fun setText(value: CharSequence) {
        binding.textView.text = value
    }

    /**
     * 设置显示的内容
     */
    private fun setText(value: Int) {
        setText(getString(value))
    }

    /**
     * TTS初始化失败
     */
    override fun onTtsInitError() {
        setText(R.string.tts_init_error)
    }

    /**
     * 汉语输出不受支持
     */
    override fun onTtsChineseNotSupported() {
        setText(R.string.tts_not_support_chinese)
    }

    /**
     * TTS准备就绪
     */
    override fun onTtsReady() {
        say(R.string.tts_ready)
        if (appLaunchHelper.isNotEmpty()) {
            say(R.string.app_list_ready, false)
        }
    }

    /**
     * 语音输出一个预设内容
     */
    private fun say(resId: Int, clear: Boolean = true) {
        say(getString(resId), clear)
    }

    /**
     * 语音输出一个文本
     */
    private fun say(text: String, clear: Boolean = true) {
        if (settings.isOpenVoice) {
            ttsHelper.say(text, clear)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.destroy()
    }

    /**
     * app列表重新排序，此时清空app显示
     */
    override fun onAppListSortChanged() {
        setText("")
    }

    /**
     * 当一个APP被选中时，我们应该显示文字，输出语音，并且震动提示
     */
    override fun onAppSelected(info: AppLaunchHelper.AppInfo?) {
        val label = info?.label ?: getString(R.string.app_selected_none)
        setText(label)
        say(label)
        vibrateHelper.startUp(VibrateHelper.Vibrate.SELECTED)
    }

    /**
     * 当用户触发语音关闭时
     * 我们需要提示用户开启方式，然后关闭语音
     */
    override fun callVoiceOff() {
        if (settings.isOpenVoice) {
            say(R.string.voice_off_hint)
            settings.isOpenVoice = false
        }
    }

    /**
     * 打开语音时，我们需要开启并且语音提示
     */
    private fun openVoice() {
        if (!settings.isOpenVoice) {
            settings.isOpenVoice = true
            say(R.string.tts_ready)
        }
    }

    /**
     * 手指按下时需要震动提示，给出反馈表示开始响应
     */
    override fun onTouchDown() {
        vibrateHelper.startUp(VibrateHelper.Vibrate.TOUCH_DOWN)
    }

    /**
     * 手指抬起时，需要给出反馈，表示响应结束
     */
    override fun onTouchUp() {
        vibrateHelper.startUp(VibrateHelper.Vibrate.TOUCH_UP)
    }

    /**
     * 当滚轮发生偏移达到阈值时，我们应该按照意图来选择下一个或者上一个app
     */
    override fun onPartitionsChange(clockwise: Boolean) {
        if (clockwise) {
            appLaunchHelper.selectNext()
        } else {
            appLaunchHelper.selectLast()
        }
    }

    /**
     * 当被双击时，需要启动选中当APP
     * 并且给出不一样的震动反馈
     */
    override fun onDoubleTap() {
        appLaunchHelper.launch()
        vibrateHelper.startUp(VibrateHelper.Vibrate.COMPLETE)
    }

}
