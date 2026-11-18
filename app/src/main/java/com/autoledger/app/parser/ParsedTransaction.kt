package com.autoledger.app.parser

import com.autoledger.app.data.TxType

/** 解析结果（纯数据，无 Android 依赖） */
data class ParsedTransaction(
    val amount: Double,
    val merchant: String,
    val type: TxType,
    val source: PaymentSource,
    /** 原始通知文本，用于生成去重 key */
    val rawText: String
)
