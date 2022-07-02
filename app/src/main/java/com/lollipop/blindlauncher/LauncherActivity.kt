package com.lollipop.blindlauncher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.blindlauncher.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity(), TtsHelper.OnInitListener {

    private val binding: ActivityLauncherBinding by lazyBind()

    private val ttsHelper by lazy {
        TtsHelper(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        ttsHelper
    }

    override fun onTtsInitError() {
//        TODO("Not yet implemented")
        Toast.makeText(this, "onTtsInitError", Toast.LENGTH_SHORT).show()
    }

    override fun onTtsChineseNotSupported() {
//        TODO("Not yet implemented")
        Toast.makeText(this, "onTtsChineseNotSupported", Toast.LENGTH_SHORT).show()
    }

    override fun onTtsReady() {
//        TODO("Not yet implemented")
        Toast.makeText(this, "onTtsReady", Toast.LENGTH_SHORT).show()
        ttsHelper.say("初始化完成了")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.destroy()
    }

}