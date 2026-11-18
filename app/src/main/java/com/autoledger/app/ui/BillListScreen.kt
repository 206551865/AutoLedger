package com.autoledger.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.autoledger.app.ui.theme.LedgerColors
import com.autoledger.app.util.formatDayHeader
import com.autoledger.app.util.formatMoney
import com.autoledger.app.util.formatTime
import com.autoledger.app.viewmodel.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    transactions: List<Transaction>,
    viewModel: LedgerViewModel,
    onAddClick: () -> Unit
) {
    val groups = remember(transactions) { viewModel.groupByDay(transactions) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账单明细", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "手动记账", tint = Color.White)
            }
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📒", fontSize = 56.sp)
                    Spacer(Modifier.size(12.dp))
                    Text("还没有账单", fontWeight = FontWeight.Medium, fontSize = 18.sp)
                    Spacer(Modifier.size(6.dp))
                    Text("开启通知权限后，支付自动入账", fontSize = 13.sp, color = Color.Gray)
                }
            }
            return@Scaffold
        }

        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            groups.forEach { (dayTs, dayTxs) ->
                val dayIncome = dayTxs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
                val dayExpense = dayTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                item(key = "header_$dayTs") {
                    Row(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDayHeader(dayTs), fontSize = 13.sp, color = Color.Gray)
                        Text(
                            buildString {
                                if (dayExpense > 0) append("支出 ¥${formatMoney(dayExpense)}")
                                if (dayIncome > 0) {
                                    if (dayExpense > 0) append("  ")
                                    append("收入 ¥${formatMoney(dayIncome)}")
                                }
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
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
            if (value == SwipeToDismissBoxValue.EndToStart) {
                viewModel.delete(tx)
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(LedgerColors.Expense).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White) }
        },
        enableDismissFromStartToEnd = false
    ) {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp).background(categoryColor(tx.category), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmoji(tx.category), fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.merchant, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1)
                Text(
                    "${tx.category} · ${tx.sourceLabel} · ${formatTime(tx.timestamp)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            val color = if (tx.type == TxType.INCOME) LedgerColors.Income else LedgerColors.Expense
            val sign = if (tx.type == TxType.INCOME) "+" else "-"
            Text(
                "$sign¥${formatMoney(tx.amount)}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private fun categoryColor(category: String): Color = when (category) {
    "餐饮" -> Color(0xFFFFF3E0)
    "交通" -> Color(0xFFE3F2FD)
    "购物" -> Color(0xFFFCE4EC)
    "生活缴费" -> Color(0xFFF1F8E9)
    "娱乐" -> Color(0xFFF3E5F5)
    "医疗" -> Color(0xFFFFEBEE)
    "教育" -> Color(0xFFEDE7F6)
    "工资收入", "理财" -> Color(0xFFE8F5E9)
    "转账红包" -> Color(0xFFFFF8E1)
    else -> Color(0xFFECEFF1)
}

private fun categoryEmoji(category: String): String = when (category) {
    "餐饮" -> "🍜"
    "交通" -> "🚕"
    "购物" -> "🛍️"
    "生活缴费" -> "💡"
    "娱乐" -> "🎬"
    "医疗" -> "💊"
    "教育" -> "📚"
    "工资收入" -> "💰"
    "理财" -> "📈"
    "转账红包" -> "🧧"
    else -> "📝"
}
