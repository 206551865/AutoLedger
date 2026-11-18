package com.autoledger.app.parser

/** 支付来源：微信 / 支付宝 / 银行 / 未知 */
enum class PaymentSource(val id: String, val label: String) {
    WECHAT("wechat", "微信支付"),
    ALIPAY("alipay", "支付宝"),
    BANK("bank", "银行卡"),
    UNKNOWN("unknown", "未知来源");

    companion object {
        /** 根据通知包名识别来源 */
        fun fromPackage(packageName: String?): PaymentSource {
            if (packageName.isNullOrBlank()) return UNKNOWN
            val p = packageName.lowercase()
            return when {
                p.contains("tencent.mm") || p.contains("wechat") -> WECHAT
                p.contains("alipay") || p.contains("eg.android") -> ALIPAY
                p.contains("bank") || p.contains("icbc") || p.contains("cmb")
                    || p.contains("ccb") || p.contains("abchina") || p.contains("boc")
                    || p.contains("psbc") || p.contains("commbank") -> BANK
                else -> UNKNOWN
            }
        }
    }
}
