package com.lollipop.blindlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.blindlauncher.databinding.ActivityHiddenListBinding
import com.lollipop.blindlauncher.databinding.ItemHiddenListAppBinding
import com.lollipop.blindlauncher.utils.AppLaunchHelper
import com.lollipop.blindlauncher.utils.FileInfoManager
import com.lollipop.blindlauncher.utils.WindowInsetsHelper
import com.lollipop.blindlauncher.utils.fixInsetsByPadding

class HiddenListActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, HiddenListActivity::class.java).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
        }
    }

    private val binding: ActivityHiddenListBinding by lazyBind()

    private val data = ArrayList<AppLaunchHelper.AppInfo>()
    private val appAdapter = AppAdapter(data, ::isAppChecked, ::onAppCheckedChanged)

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = appAdapter
        loadData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        val appList = AppLaunchHelper.getAppList(this, false)

        data.clear()
        data.addAll(appList)
        appAdapter.notifyDataSetChanged()
    }

    private fun isAppChecked(info: AppLaunchHelper.AppInfo): Boolean {
        TODO()
    }

    private fun onAppCheckedChanged(info: AppLaunchHelper.AppInfo, checked: Boolean) {
        TODO()
    }

    private class HiddenInfoManager: FileInfoManager() {

        override fun init(context: Context) {
            TODO("Not yet implemented")
        }

    }

    private class AppAdapter(
        private val data: List<AppLaunchHelper.AppInfo>,
        private val isChecked: (AppLaunchHelper.AppInfo) -> Boolean,
        private val onAppCheckedChanged: (AppLaunchHelper.AppInfo, Boolean) -> Unit
    ) : RecyclerView.Adapter<AppHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder(parent.bind(false), ::onItemChecked)
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            val info = data[position]
            holder.bind(info, isChecked(info))
        }

        private fun onItemChecked(position: Int, isChecked: Boolean) {
            if (position < 0 || position >= data.size) {
                return
            }
            onAppCheckedChanged(data[position], isChecked)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private class AppHolder(
        private val binding: ItemHiddenListAppBinding,
        private val onCheckedChangedCallback: (Int, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var callbackLock = false

        init {
            binding.appSwitch.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(isChecked)
            }
        }

        private fun onCheckedChanged(isChecked: Boolean) {
            if (callbackLock) {
                return
            }
            onCheckedChangedCallback(adapterPosition, isChecked)
        }

        fun bind(info: AppLaunchHelper.AppInfo, isChecked: Boolean) {
            binding.appName.text = info.label
            callbackLock = true
            binding.appSwitch.isChecked = isChecked
            callbackLock = false
        }

    }

}