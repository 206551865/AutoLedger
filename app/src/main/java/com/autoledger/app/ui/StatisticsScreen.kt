package com.autoledger.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.data.Transaction
import com.autoledger.app.ui.theme.LedgerColors
import com.autoledger.app.util.formatMoney
import com.autoledger.app.viewmodel.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(transactions: List<Transaction>, viewModel: LedgerViewModel) {
    val summary = remember(transactions) { viewModel.monthlySummary(transactions) }
    val months = remember(transactions) { viewModel.last6Months(transactions) }
    val maxAmount = remember(months) {
        maxOf(1.0, months.maxOf { maxOf(it.income, it.expense) })
    }
    val catMax = remember(summary) { summary.categoryBreakdown.firstOrNull()?.second ?: 1.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 本月概览卡片
            Box(
                Modifier.fillMaxWidth().background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                ).padding(20.dp)
            ) {
                Column {
                    Text("本月概览", color = Color.White.copy(0.85f), fontSize = 14.sp)
                    Spacer(Modifier.size(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("收入", color = Color.White.copy(0.85f), fontSize = 13.sp)
                            Text("¥${formatMoney(summary.income)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("支出", color = Color.White.copy(0.85f), fontSize = 13.sp)
                            Text("¥${formatMoney(summary.expense)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        "本月共 ${summary.count} 笔 · 结余 ¥${formatMoney(summary.income - summary.expense)}",
                        color = Color.White.copy(0.9f), fontSize = 13.sp
                    )
                }
            }

            // 近 6 个月趋势
            SectionCard("近 6 个月收支趋势") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendDot(LedgerColors.Income, "收入")
                    LegendDot(LedgerColors.Expense, "支出")
                }
                Spacer(Modifier.size(8.dp))
                Canvas(Modifier.fillMaxWidth().height(180.dp)) {
                    val groupCount = months.size
                    val groupWidth = size.width / groupCount
                    val barWidth = groupWidth * 0.28f
                    val chartHeight = size.height - 30f
                    months.forEachIndexed { i, m ->
                        val centerX = groupWidth * i + groupWidth / 2
                        val incomeH = (m.income / maxAmount * chartHeight).toFloat()
                        val expenseH = (m.expense / maxAmount * chartHeight).toFloat()
                        drawRect(
                            color = LedgerColors.Income,
                            topLeft = Offset(centerX - barWidth - 2f, chartHeight - incomeH),
                            size = Size(barWidth, incomeH)
                        )
                        drawRect(
                            color = LedgerColors.Expense,
                            topLeft = Offset(centerX + 2f, chartHeight - expenseH),
                            size = Size(barWidth, expenseH)
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    months.forEach { Text(it.label, fontSize = 12.sp, color = Color.Gray) }
                }
            }

            // 本月支出分类占比
            SectionCard("本月支出分类") {
                if (summary.categoryBreakdown.isEmpty()) {
                    Text("本月暂无支出", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
                } else {
                    Spacer(Modifier.size(12.dp))
                    summary.categoryBreakdown.forEach { (cat, amount) ->
                        val ratio = (amount / summary.expense).let { if (it.isNaN()) 0f else it }
                        Column(Modifier.padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$cat  ${String.format("%.0f", ratio * 100)}%", fontSize = 13.sp)
                                Text("¥${formatMoney(amount)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.size(4.dp))
                            Box(
                                Modifier.fillMaxWidth().height(8.dp).background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                            ) {
                                Box(
                                    Modifier.fillMaxWidth(ratio.coerceIn(0f, 1f)).height(8.dp).background(
                                        categoryBarColor(cat),
                                        RoundedCornerShape(4.dp)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(16.dp)
        ).padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        content()
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

private fun categoryBarColor(category: String): Color = when (category) {
    "餐饮" -> Color(0xFFFF9800)
    "交通" -> Color(0xFF2196F3)
    "购物" -> Color(0xFFE91E63)
    "生活缴费" -> Color(0xFF8BC34A)
    "娱乐" -> Color(0xFF9C27B0)
    "医疗" -> Color(0xFFF44336)
    "教育" -> Color(0xFF673AB7)
    else -> Color(0xFF607D8B)
}
