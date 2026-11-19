package com.autoledger.app.util

data class ReleaseNote(
    val version: String,
    val code: Int,
    val date: String,
    val notes: List<String>
)

/**
 * 应用内更新记录。
 * 每次发布新版本时：在 CHANGELOG.md 登记的同时，在此处追加一条。
 */
object AppChangelog {
    val releases = listOf(
        ReleaseNote(
            version = "1.1.0",
            code = 2,
            date = "2026-11-19",
            notes = listOf(
                "应用名称改为「有数记账」",
                "按有数设计稿重做界面：总览 / 分析 / 凸起 + / 账本 / 我的",
                "新增 AI 一句话记账、通知诊断、来源开关",
                "新增版本记录，交付 APK 按版本号命名"
            )
        ),
        ReleaseNote(
            version = "1.0.0",
            code = 1,
            date = "2026-11-18",
            notes = listOf(
                "首个版本：通知监听自动记账（微信 / 支付宝 / 银行卡）",
                "本地 SQLite 存储、账单列表、支出统计、手动补记",
                "开机自启与重复通知去重"
            )
        )
    )
}
