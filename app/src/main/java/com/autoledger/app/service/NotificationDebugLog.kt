package com.autoledger.app.service

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 通知诊断日志：记录每一条被监听到的支付类通知原文及处理结果。
 * 仅用于排查「为什么某条通知没记账」，保存在本地 SharedPreferences，最多保留 50 条。
 */
object NotificationDebugLog {

    private const val PREFS = "notif_debug_log"
    private const val KEY_LOGS = "logs"
    private const val KEY_LAST_CONNECTED = "last_connected"
    private const val MAX_LOGS = 50

    data class Entry(
        val time: Long,
        val pkg: String,
        val content: String,
        val result: String,      // 处理结论，如「已记账」「无金额，丢弃」
        val ok: Boolean
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** 追加一条日志，超出上限丢弃最旧的 */
    fun add(context: Context, pkg: String, content: String, result: String, ok: Boolean) {
        runCatching {
            val list = readRaw(context)
            val entry = JSONObject().apply {
                put("time", System.currentTimeMillis())
                put("pkg", pkg)
                put("content", content)
                put("result", result)
                put("ok", ok)
            }
            list.put(0, entry)
            while (list.length() > MAX_LOGS) list.remove(list.length() - 1)
            prefs(context).edit().putString(KEY_LOGS, list.toString()).apply()
        }
    }

    /** 读取全部日志，时间倒序 */
    fun read(context: Context): List<Entry> {
        val result = mutableListOf<Entry>()
        runCatching {
            val arr = readRaw(context)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(
                    Entry(
                        time = o.optLong("time"),
                        pkg = o.optString("pkg"),
                        content = o.optString("content"),
                        result = o.optString("result"),
                        ok = o.optBoolean("ok")
                    )
                )
            }
        }
        return result
    }

    /** 记录监听服务连接成功的时间戳 */
    fun markConnected(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_CONNECTED, System.currentTimeMillis()).apply()
    }

    /** 最近一次连接时间，0 表示从未连接 */
    fun lastConnected(context: Context): Long =
        prefs(context).getLong(KEY_LAST_CONNECTED, 0L)

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_LOGS).apply()
    }

    private fun readRaw(context: Context): JSONArray {
        val s = prefs(context).getString(KEY_LOGS, null) ?: return JSONArray()
        return runCatching { JSONArray(s) }.getOrDefault(JSONArray())
    }
}
