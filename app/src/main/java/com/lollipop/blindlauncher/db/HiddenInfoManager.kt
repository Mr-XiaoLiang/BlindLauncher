package com.lollipop.blindlauncher.db

import android.content.Context
import com.lollipop.blindlauncher.doAsync
import com.lollipop.blindlauncher.onUI
import com.lollipop.blindlauncher.utils.FileInfoManager
import org.json.JSONArray
import java.io.File

object HiddenInfoManager : FileInfoManager() {

    private const val FILE_NAME = "hide.lpf"

    private var packageList = HashSet<String>()

    private var file: File? = null

    private var needUpdate = true

    var mode = 0
        private set

    fun has(pkg: String): Boolean {
        return packageList.contains(pkg)
    }

    fun remove(pkg: String) {
        packageList.remove(pkg)
        modeUpdate()
    }

    fun add(pkg: String) {
        packageList.add(pkg)
        modeUpdate()
    }

    fun save() {
        val f = file ?: return
        doAsync {
            val jsonArray = JSONArray()
            packageList.forEach {
                jsonArray.put(it)
            }
            writeToFile(jsonArray, f)
        }
    }

    private fun modeUpdate() {
        mode++
    }

    fun load(complete: () -> Unit) {
        val f = file
        if (f == null) {
            needUpdate = true
            complete()
            return
        }
        if (!needUpdate) {
            complete()
            return
        }
        doAsync {
            val tempSet = HashSet<String>()
            readArrayFromFile(
                file = f,
                emptyData = {}
            ) {
                if (it is String) {
                    tempSet.add(it)
                }
            }
            onUI {
                packageList.clear()
                packageList.addAll(tempSet)
                needUpdate = false
                modeUpdate()
                complete()
            }
        }
    }

    override fun init(context: Context) {
        file = File(context.filesDir, FILE_NAME)
    }

}