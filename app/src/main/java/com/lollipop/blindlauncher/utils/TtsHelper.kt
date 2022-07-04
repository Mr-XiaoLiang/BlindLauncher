package com.lollipop.blindlauncher.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*
import kotlin.math.min

/**
 * 文字转语音的工具，使用系统提供的工具，降低开发成本
 */
class TtsHelper(
    context: Context,
    private val errorListener: OnInitListener
) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context, this)

    private val maxSpeechLength by lazy {
        TextToSpeech.getMaxSpeechInputLength()
    }

    /**
     * 如果初始化失败，那么就不要使用来
     */
    var isActive = false
        private set

    /**
     * 输出一段文本内容
     * @param clear 如果为true，那么将会中止当前的输出内容，并且替换为新的内容
     */
    fun say(text: String, clear: Boolean = true) {
        if (!isActive || text.isEmpty()) {
            return
        }
        if (tts.isSpeaking && clear) {
            tts.stop()
        }
        val textLength = text.length
        var speakIndex = 0
        while (speakIndex < textLength) {
            val speakEnd = min(speakIndex + maxSpeechLength, textLength)
            val content = text.substring(speakIndex, speakEnd)
            speakIndex = speakEnd
            tts.speak(
                content,
                if (speakIndex == 0 && clear) {
                    TextToSpeech.QUEUE_FLUSH
                } else {
                    TextToSpeech.QUEUE_ADD
                },
                null,
                "${System.currentTimeMillis().toString(16)}-${speakIndex}"
            )
        }
    }

    override fun onInit(status: Int) {
        isActive = false
        when (status) {
            TextToSpeech.SUCCESS -> {

                when (setLanguage()) {
                    TextToSpeech.LANG_MISSING_DATA,
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        tts.language = Locale.getDefault()
                        errorListener.onTtsChineseNotSupported()
                    }
                    TextToSpeech.LANG_AVAILABLE -> {
                        isActive = true
                        errorListener.onTtsReady()
                    }
                }
            }
            else -> {
                errorListener.onTtsInitError()
            }
        }
    }

    private fun setLanguage(): Int {
        val languages = tts.availableLanguages
        if (languages.isEmpty()) {
            return TextToSpeech.LANG_NOT_SUPPORTED
        }
        if (languages.contains(Locale.CHINESE)) {
            if (tts.setLanguage(Locale.CHINESE) == TextToSpeech.LANG_AVAILABLE) {
                return TextToSpeech.LANG_AVAILABLE
            }
        }
        if (languages.contains(Locale.CHINA)) {
            if (tts.setLanguage(Locale.CHINA) == TextToSpeech.LANG_AVAILABLE) {
                return TextToSpeech.LANG_AVAILABLE
            }
        }
        return TextToSpeech.LANG_NOT_SUPPORTED
    }

    /**
     * 销毁并释放资源
     */
    fun destroy() {
        // 打断朗读
        tts.stop()
        // 释放资源
        tts.shutdown()
    }

    interface OnInitListener {
        fun onTtsInitError()
        fun onTtsChineseNotSupported()
        fun onTtsReady()
    }

}