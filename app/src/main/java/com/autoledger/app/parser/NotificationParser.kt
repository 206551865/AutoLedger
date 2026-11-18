package com.autoledger.app.parser

import com.autoledger.app.data.TxType

/**
 * 支付通知解析引擎（纯 Kotlin，无 Android 依赖，可在 JVM 上单元测试）。
 *
 * 设计原则：
 * 1. 多级金额正则，兼容 ¥ / ￥ / 元 / 数字 等多种格式；
 * 2. 收支方向用「强信号优先」判定，避免「微信支付收款」中同时出现收/支词误判；
 * 3. 商户名按多组模式提取，取不到时回退到来源标签；
 * 4. 非支付类通知（如纯验证码、广告）通过金额缺失自动过滤。
 */
object NotificationParser {

    /** 金额正则：按优先级排列，捕获组 1 为数字 */
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:¥|￥)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)"""),       // ¥123.45 / ￥1,234
        Regex("""([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*元"""),             // 123.45元
        Regex("""(?:金额|转账金额|支付|消费|付款|收入|收款|余额变动|转账|零钱变动)[^0-9]{0,8}([0-9][0-9,]*(?:\.[0-9]{1,2})?)"""), // 金额123.45 / 转账¥100
        Regex("""(?<!\d)([0-9]{1,6}\.[0-9]{2})(?!\d)""")                // 兜底：独立的两位小数
    )

    /** 强收入信号（命中即判收入，优先于一切支出词） */
    private val STRONG_INCOME = listOf(
        "收款通知", "收到转账", "转账收入", "收款成功", "收入人民币", "收入¥", "收入￥",
        "已收款", "退款成功", "退款到账", "转入", "入账", "返还", "报销到账", "工资到账",
        "余额宝收益", "收益到账", "收到红包", "红包收入", "向你转账", "给你转账"
    )

    /** 强支出信号 */
    private val STRONG_EXPENSE = listOf(
        "付款成功", "支付成功", "消费", "支出", "扣款", "已支付", "付款金额", "支付金额",
        "转账支出", "向.*支付", "付款给", "扣费", "支付¥", "支付￥", "已扣款", "还款", "缴费"
    )

    /** 弱收入词（无强信号时参考） */
    private val WEAK_INCOME = listOf("收款", "收到", "入账", "退款", "返现", "红包")

    /** 商户名提取模式：捕获组 1 为商户 */
    private val MERCHANT_PATTERNS = listOf(
        Regex("""(?:收款方|商户名称|商户|商家|付款给|转账给|向)\s*[:：]?\s*([^\s,，。；;]{2,24})"""),
        Regex("""在\s*([^\s,，。；;]{2,24})\s*(?:消费|付款|支付)"""),
        Regex("""([^\s,，。；;]{2,24})\s*(?:消费|收款|付款)"""),
        Regex("""(?:来自|来自用户|付款人)\s*[:：]?\s*([^\s,，。；;]{2,24})""")
    )

    /** 明确不是支付通知的关键词（用于降噪）。
     *  注意：不要放「优惠」「红包」这类词——微信支付通知常带「支付有优惠」字样，会误杀真实账目。 */
    private val NOISE_KEYWORDS = listOf("验证码", "校验码", "登录密码", "广告推送", "活动推荐")

    /**
     * 解析一条通知。
     * @param text 通知正文（title + content 拼接后的完整文本）
     * @param packageName 来源 App 包名
     * @return 解析成功返回 [ParsedTransaction]，金额缺失或识别失败返回 null
     */
    fun parse(text: String, packageName: String? = null): ParsedTransaction? {
        if (text.isBlank()) return null
        val normalized = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()

        if (NOISE_KEYWORDS.any { normalized.contains(it) }) return null

        val amount = extractAmount(normalized) ?: return null
        if (amount <= 0.0) return null

        val type = detectDirection(normalized)
        val merchant = extractMerchant(normalized)
        val source = PaymentSource.fromPackage(packageName)
            .let { if (it == PaymentSource.UNKNOWN) detectSourceFromText(normalized) else it }

        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            type = type,
            source = source,
            rawText = normalized
        )
    }

    /** 从文本中提取第一个合理金额 */
    private fun extractAmount(text: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            val value = raw.toDoubleOrNull() ?: continue
            // 过滤掉明显不合理的金额（如电话号码片段、年份）
            if (value > 0.0 && value < 10_000_000.0) return value
        }
        return null
    }

    /** 判定收支方向：强收入 > 强支出 > 弱收入 > 默认支出 */
    private fun detectDirection(text: String): TxType {
        if (STRONG_INCOME.any { text.contains(it) }) return TxType.INCOME
        if (STRONG_EXPENSE.any { regexOrText ->
                if (regexOrText.contains(".*")) Regex(regexOrText).containsMatchIn(text)
                else text.contains(regexOrText)
            }) return TxType.EXPENSE
        if (WEAK_INCOME.any { text.contains(it) }) return TxType.INCOME
        return TxType.EXPENSE
    }

    /** 提取商户/对方名称 */
    private fun extractMerchant(text: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val candidate = match.groupValues[1]
                .trim('：', ':', ' ', '，', ',', '。', '.')
            if (candidate.length >= 2 && !candidate.all { it.isDigit() }) return candidate
        }
        return ""
    }

    /** 包名未知时，从文本关键词兜底判断来源 */
    private fun detectSourceFromText(text: String): PaymentSource = when {
        text.contains("微信支付") || text.contains("微信") -> PaymentSource.WECHAT
        text.contains("支付宝") -> PaymentSource.ALIPAY
        text.contains("银行") || text.contains("储蓄卡") || text.contains("信用卡") -> PaymentSource.BANK
        else -> PaymentSource.UNKNOWN
    }

    /**
     * 解释一条通知为什么解析失败（诊断用）。
     * 与 parse() 的失败路径保持一致。
     */
    fun explainFailure(text: String): String {
        if (text.isBlank()) return "通知内容为空"
        val normalized = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        val hitNoise = NOISE_KEYWORDS.firstOrNull { normalized.contains(it) }
        if (hitNoise != null) return "被降噪词「$hitNoise」过滤"
        val amount = extractAmount(normalized)
        if (amount == null) return "未识别到金额（通知正文里可能没有写金额，微信好友转账常见此情况）"
        if (amount <= 0.0) return "金额无效"
        return "其他原因"
    }
}
