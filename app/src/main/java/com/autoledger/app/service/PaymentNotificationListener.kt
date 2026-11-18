package com.autoledger.app.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.autoledger.app.AutoLedgerApp
import com.autoledger.app.data.CategoryRules
import com.autoledger.app.data.Transaction
import com.autoledger.app.data.TxType
import com.autoledger.app.parser.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 通知监听服务：App 的核心入口。
 * 用户授予「通知使用权」后，系统把所有通知投递到 onNotificationPosted，
 * 本服务过滤出支付类通知，解析后写入本地数据库。
 */
class PaymentNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "通知监听服务已连接")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn ?: return
        val packageName = notification.packageName ?: return

        // 只处理已启用的来源，避免误记其他 App
        if (!SourceToggle.isPackageEnabled(packageName, this)) return

        val extras: Bundle = notification.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val content = listOf(title, text, bigText).filter { it.isNotBlank() }.joinToString(" ")

        if (content.isBlank()) return

        val parsed = NotificationParser.parse(content, packageName) ?: return

        val timestamp = notification.postTime
        // 去重 key：来源 + 金额 + 商户 + 分钟级时间，防止同一次支付重复通知入库
        val minuteBucket = timestamp / 60_000
        val dedupKey = "${parsed.source.id}|${parsed.amount}|${parsed.merchant}|$minuteBucket"

        val merchant = parsed.merchant.ifBlank { parsed.source.label }
        val isIncome = parsed.type == TxType.INCOME
        val category = CategoryRules.classify(merchant, isIncome)

        val tx = Transaction(
            amount = parsed.amount,
            merchant = merchant,
            type = parsed.type,
            category = category,
            timestamp = timestamp,
            source = parsed.source.id,
            sourceLabel = parsed.source.label,
            dedupKey = dedupKey
        )

        val dao = (application as AutoLedgerApp).database.transactionDao()
        serviceScope.launch {
            // 二次校验去重（数据库唯一索引 + 查询双保险）
            if (dao.findByDedupKey(dedupKey) != null) {
                Log.d(TAG, "重复通知，跳过: $dedupKey")
                return@launch
            }
            dao.insert(tx)
            Log.i(TAG, "已记账: ${tx.sourceLabel} ${tx.amount} ${tx.merchant} [$category]")
        }
    }

    companion object {
        private const val TAG = "NotifListener"
    }
}
