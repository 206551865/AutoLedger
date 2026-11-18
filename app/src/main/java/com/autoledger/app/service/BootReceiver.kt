package com.autoledger.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 开机/应用升级后的唤醒接收器。
 * 说明：Android 系统会在设备重启后自动重新绑定用户已授权的通知监听服务，
 * 因此这里不需要手动 bindService（系统也不允许三方 App 主动绑定该服务）。
 * 本 Receiver 记录状态，并在未来需要时提供扩展点（如重置监听、发通知提醒用户）。
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val granted = SourceToggle.isNotificationAccessGranted(context)
            Log.i(TAG, "开机/升级广播: $action, 通知使用权已授权=$granted")
            // 扩展点：若未授权可在此发一条通知引导用户前往设置页开启
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
