package com.autoledger.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.data.Transaction
import com.autoledger.app.ui.theme.YS
import com.autoledger.app.util.formatMoney
import com.autoledger.app.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(transactions: List<Transaction>, viewModel: LedgerViewModel) {
    val summary = remember(transactions) { viewModel.monthlySummary(transactions) }
    val monthText = SimpleDateFormat("M 月", Locale.getDefault()).format(Date())

    val topCats = summary.categoryBreakdown.take(5)
    val totalExpense = summary.expense
    val palette = listOf(YS.brand, YS.expense, YS.blue, YS.sand, YS.inkFaint)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // 标题 + 月份
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("支出分析", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = YS.ink)
            Box(Modifier.background(YS.surface, RoundedCornerShape(100))
                .border(1.dp, YS.line, RoundedCornerShape(100))
                .padding(horizontal = 11.dp, vertical = 7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(monthText, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = YS.inkSoft)
                    Spacer(Modifier.width(5.dp))
                    Text("▾", fontSize = 11.sp, color = YS.inkSoft)
                }
            }
        }

        // 总支出
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("本月总支出", fontSize = 11.sp, color = YS.inkFaint)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("¥ ", fontSize = 20.sp, color = YS.ink, fontFamily = YS.fontNum)
                Text(formatMoney(totalExpense), fontFamily = YS.fontNum, fontSize = 34.sp,
                    fontWeight = FontWeight.Medium, color = YS.ink)
            }
        }

        // 甜甜圈 + 图例
        if (topCats.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Text("本月还没有支出记录", fontSize = 12.sp, color = YS.inkFaint)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(topCats.map { it.second }, palette)
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    topCats.forEachIndexed { i, (cat, amount) ->
                        val pct = if (totalExpense > 0) (amount / totalExpense * 100).toInt() else 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(palette[i], RoundedCornerShape(3.dp)))
                            Spacer(Modifier.width(7.dp))
                            Text(cat, fontSize = 10.5.sp, color = YS.inkSoft, modifier = Modifier.weight(1f))
                            Text("$pct%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = YS.ink,
                                fontFamily = YS.fontNum)
                        }
                    }
                }
            }

            // 支出排行
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                Text("支出排行", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = YS.ink)
                Spacer(Modifier.height(14.dp))
                val maxAmt = topCats.firstOrNull()?.second ?: 1.0
                topCats.forEachIndexed { i, (cat, amount) ->
                    val pct = if (totalExpense > 0) (amount / totalExpense * 100).toInt() else 0
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat, fontSize = 11.sp, color = YS.inkSoft)
                        Text("¥ ${formatMoney(amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = YS.ink, fontFamily = YS.fontNum)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).background(YS.surface3, RoundedCornerShape(100))) {
                        Box(Modifier.fillMaxWidth((amount / maxAmt).coerceIn(0.0, 1.0).toFloat())
                            .height(6.dp).background(palette[i], RoundedCornerShape(100)))
                    }
                    Spacer(Modifier.height(13.dp))
                }
            }

            // AI 提示
            Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
                Row(Modifier.background(YS.brandTint, YS.rCard).padding(14.dp)) {
                    Text("✦", color = YS.brand, fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    val top = topCats.first()
                    val pct = if (totalExpense > 0) (top.second / totalExpense * 100).toInt() else 0
                    Text(
                        "「${top.first}」占本月支出 ${pct}%，是当前最大的开支项。合理规划这一项，能让结余更可观。",
                        fontSize = 10.5.sp, color = YS.inkSoft, lineHeight = 17.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun DonutChart(amounts: List<Double>, colors: List<Color>) {
    val total = amounts.sum()
    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(132.dp)) {
            val stroke = 15.dp.toPx()
            val radius = (size.minDimension - stroke) / 2
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)
            // 底环
            drawArc(
                color = YS.surface3,
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            if (total > 0) {
                var start = -90f
                amounts.forEachIndexed { i, amount ->
                    val sweep = (amount / total * 360).toFloat()
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = start, sweepAngle = sweep, useCenter = false,
                        topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val topPct = if (total > 0) (amounts.maxOrNull()!! / total * 100).toInt() else 0
            Text("$topPct%", fontFamily = YS.fontNum, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = YS.ink)
            Text("最大占比", fontSize = 9.sp, color = YS.inkFaint)
        }
    }
}
