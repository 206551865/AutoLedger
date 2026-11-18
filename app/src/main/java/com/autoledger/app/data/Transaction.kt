package com.autoledger.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 一条账单记录。
 * [dedupKey] 用于通知去重：由来源包名+金额+商户+时间分钟生成，避免同一条通知重复触发入库。
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["dedupKey"], unique = true), Index(value = ["timestamp"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,
    val merchant: String,
    val type: TxType,
    val category: String,
    val timestamp: Long,
    /** 来源：wechat / alipay / bank_xxx / manual */
    val source: String,
    /** 来源显示名，如「微信支付」 */
    val sourceLabel: String,
    val dedupKey: String,
    val note: String = ""
)
