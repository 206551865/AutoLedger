package com.autoledger.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.data.Transaction
import com.autoledger.app.data.TxType
import com.autoledger.app.ui.theme.YS
import com.autoledger.app.util.formatDayHeader
import com.autoledger.app.util.formatMoney
import com.autoledger.app.util.formatTime
import com.autoledger.app.viewmodel.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(transactions: List<Transaction>, viewModel: LedgerViewModel) {
    val groups = remember(transactions) { viewModel.groupByDay(transactions) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "账本",
            fontSize = 19.sp, fontWeight = FontWeight.Bold, color = YS.ink,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
        )
        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📒", fontSize = 52.sp)
                    Spacer(Modifier.size(12.dp))
                    Text("还没有账单", fontWeight = FontWeight.Medium, fontSize = 17.sp, color = YS.ink)
                    Spacer(Modifier.size(6.dp))
                    Text("开启通知权限后，支付自动入账", fontSize = 13.sp, color = YS.inkFaint)
                }
            }
            return@Column
        }
        LazyColumn(Modifier.fillMaxSize()) {
            groups.forEach { (dayTs, dayTxs) ->
                val dayIncome = dayTxs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
                val dayExpense = dayTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                item(key = "h_$dayTs") {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDayHeader(dayTs), fontSize = 12.sp, color = YS.inkFaint)
                        Text(
                            buildString {
                                if (dayExpense > 0) append("支出 ${formatMoney(dayExpense)}")
                                if (dayIncome > 0) {
                                    if (dayExpense > 0) append("  ")
                                    append("收入 ${formatMoney(dayIncome)}")
                                }
                            },
                            fontSize = 11.sp, color = YS.inkFaint
                        )
                    }
                }
                items(dayTxs, key = { it.id }) { tx -> TransactionRow(tx, viewModel) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionRow(tx: Transaction, viewModel: LedgerViewModel) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { viewModel.delete(tx); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(YS.expense).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White) }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            Modifier.fillMaxWidth().background(YS.surface)
                .padding(horizontal = 22.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(tx.category, 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.merchant, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = YS.ink, maxLines = 1)
                Text(
                    "${tx.category} · ${tx.sourceLabel} · ${formatTime(tx.timestamp)}",
                    fontSize = 10.5.sp, color = YS.inkFaint, maxLines = 1
                )
            }
            val isIncome = tx.type == TxType.INCOME
            Text(
                "${if (isIncome) "+" else "−"}${formatMoney(tx.amount)}",
                color = if (isIncome) YS.income else YS.ink,
                fontWeight = FontWeight.Bold,
                fontFamily = YS.fontNum,
                fontSize = 15.sp
            )
        }
    }
}
