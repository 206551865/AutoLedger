package com.autoledger.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoledger.app.AutoLedgerApp
import com.autoledger.app.data.CategoryRules
import com.autoledger.app.data.Transaction
import com.autoledger.app.data.TxType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as AutoLedgerApp).database.transactionDao()

    val allTransactions: StateFlow<List<Transaction>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addManual(amount: Double, merchant: String, type: TxType, category: String, note: String = "") {
        if (amount <= 0.0) return
        val now = System.currentTimeMillis()
        val dedupKey = "manual|$amount|$merchant|${now / 1000}"
        viewModelScope.launch {
            dao.insert(
                Transaction(
                    amount = amount,
                    merchant = merchant.ifBlank { "手动记账" },
                    type = type,
                    category = category.ifBlank { CategoryRules.classify(merchant, type == TxType.INCOME) },
                    timestamp = now,
                    source = "manual",
                    sourceLabel = "手动记账",
                    dedupKey = dedupKey,
                    note = note
                )
            )
        }
    }

    fun delete(tx: Transaction) {
        viewModelScope.launch { dao.delete(tx) }
    }

    fun clearAll() {
        viewModelScope.launch { dao.deleteAll() }
    }

    /** 按自然日分组，日期倒序 */
    fun groupByDay(txs: List<Transaction>): List<Pair<Long, List<Transaction>>> =
        txs.groupBy { startOfDay(it.timestamp) }
            .toList()
            .sortedByDescending { it.first }

    /** 本月统计 */
    fun monthlySummary(txs: List<Transaction>): Summary {
        val (from, to) = currentMonthRange()
        val monthTxs = txs.filter { it.timestamp in from until to }
        val income = monthTxs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
        val expense = monthTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
        val byCategory = monthTxs
            .filter { it.type == TxType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
        return Summary(income, expense, byCategory, monthTxs.size)
    }

    /** 最近 6 个月的收支趋势 */
    fun last6Months(txs: List<Transaction>): List<MonthPoint> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val result = mutableListOf<MonthPoint>()
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance().apply { timeInMillis = cal.timeInMillis; add(Calendar.MONTH, -i) }
            val from = c.timeInMillis
            val to = Calendar.getInstance().apply { timeInMillis = from; add(Calendar.MONTH, 1) }.timeInMillis
            val monthTxs = txs.filter { it.timestamp in from until to }
            result.add(
                MonthPoint(
                    label = "${c.get(Calendar.MONTH) + 1}月",
                    income = monthTxs.filter { it.type == TxType.INCOME }.sumOf { it.amount },
                    expense = monthTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                )
            )
        }
        return result
    }

    private fun startOfDay(ts: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis
        val to = Calendar.getInstance().apply { timeInMillis = from; add(Calendar.MONTH, 1) }.timeInMillis
        return from to to
    }

    data class Summary(
        val income: Double,
        val expense: Double,
        val categoryBreakdown: List<Pair<String, Double>>,
        val count: Int
    )

    data class MonthPoint(val label: String, val income: Double, val expense: Double)
}
