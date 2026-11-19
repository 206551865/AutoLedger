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
            version = "1.3.0",
            code = 4,
            date = "2026-11-19",
            notes = listOf(
                "修复 AI 页按系统返回键会退出 App 的问题",
                "新增「点击说话」语音记账，识别后自动填入解析",
                "顶部返回按钮更好点，消除兼容警告"
            )
        ),
        ReleaseNote(
            version = "1.2.0",
            code = 3,
            date = "2026-11-19",
            notes = listOf(
                "修复 AI 记账口语解析失败（如「午饭 星巴克 38 块」）",
                "修复银行通知把余额误当消费金额",
                "总览页收支标题按实际正负显示",
                "AI 记账结果提示自动消失，失败时给出引导"
            )
        ),
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
