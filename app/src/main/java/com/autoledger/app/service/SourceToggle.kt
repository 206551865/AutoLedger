package com.autoledger.app.service

import android.content.Context
import android.content.SharedPreferences

/**
 * 来源开关：控制哪些 App 的通知会被自动记账。
 * 基于 SharedPreferences，默认全部启用。
 */
object SourceToggle {

    private const val PREFS = "source_toggles"
    private const val KEY_PREFIX = "enabled_"

    /** 已知来源的包名与显示名（含常见银行 App 包名片段） */
    val KNOWN_SOURCES = listOf(
        SourceDef("wechat", "微信支付", listOf("com.tencent.mm")),
        SourceDef("alipay", "支付宝", listOf("com.eg.android.AlipayGphone")),
        SourceDef("bank", "银行卡", listOf("cmbchina", "icbc", "ccb", "abchina", "boc", "psbc", "commbank"))
    )

    data class SourceDef(val id: String, val label: String, val packageKeywords: List<String>)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** 判断某包名对应的来源是否启用（默认 true）。未知来源返回 false，避免误记。 */
    fun isPackageEnabled(packageName: String, context: Context): Boolean {
        val def = KNOWN_SOURCES.firstOrNull { src ->
            src.packageKeywords.any { packageName.lowercase().contains(it.lowercase()) }
        } ?: return false
        return prefs(context).getBoolean(KEY_PREFIX + def.id, true)
    }

    /** 按来源 id 判断是否启用（供设置页使用） */
    fun isSourceEnabled(sourceId: String, context: Context): Boolean =
        prefs(context).getBoolean(KEY_PREFIX + sourceId, true)

    fun setEnabled(sourceId: String, enabled: Boolean, context: Context) {
        prefs(context).edit().putBoolean(KEY_PREFIX + sourceId, enabled).apply()
    }

    /** 判断通知使用权是否已授予 */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabled = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        return enabled.contains(context.packageName)
    }
}
