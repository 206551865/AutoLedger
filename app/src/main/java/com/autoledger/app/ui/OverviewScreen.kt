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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.data.Transaction
import com.autoledger.app.data.TxType
import com.autoledger.app.ui.theme.YS
import com.autoledger.app.util.formatDayHeader
import com.autoledger.app.util.formatMoney
import com.autoledger.app.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun OverviewScreen(
    transactions: List<Transaction>,
    viewModel: LedgerViewModel,
    onSeeAll: () -> Unit
) {
    val summary = remember(transactions) { viewModel.monthlySummary(transactions) }
    val net = summary.expense - summary.income
    val balance = summary.income - summary.expense
    val dateText = SimpleDateFormat("M 月 d 日 · EEEE", Locale.getDefault()).format(Date())
    val greet = greetingText()

    val recent = remember(transactions) {
        transactions.sortedByDescending { it.timestamp }.take(4)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        // 问候 + 头像
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(dateText, fontSize = 12.sp, color = YS.inkFaint)
                Text(
                    "$greet，你好",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = YS.ink,
                    letterSpacing = (-0.01).sp
                )
            }
            Box(
                Modifier.size(38.dp).background(YS.brandTint, YS.rIcon),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = YS.brand, modifier = Modifier.size(20.dp))
                Box(
                    Modifier.size(7.dp).offset(x = 11.dp, y = (-9).dp)
                        .background(YS.expense, CircleShape)
                )
            }
        }

        // Hero 卡片
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                .background(YS.brand, YS.rHero)
                .padding(22.dp)
        ) {
            Column {
                Text("本月净支出", fontSize = 11.sp, color = Color.White.copy(0.75f), fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 22.sp, color = Color.White.copy(0.8f), modifier = Modifier.padding(bottom = 5.dp))
                    Text(
                        formatMoney(kotlin.math.abs(net)),
                        fontFamily = YS.fontNum,
                        fontSize = 42.sp,
                        color = Color.White,
                        lineHeight = 46.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.background(Color.White.copy(0.14f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        if (balance >= 0) "↑  本月结余为正，继续保持" else "本月支出超出收入",
                        fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    HeroStat("收入", "¥ ${formatMoney(summary.income)}")
                    HeroStat("结余", "¥ ${formatMoney(balance)}")
                }
            }
        }

        // AI 洞察卡
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                .background(YS.brandTint, YS.rCard)
                .padding(15.dp)
        ) {
            Row {
                Box(Modifier.size(32.dp).background(YS.brand, YS.rIcon), contentAlignment = Alignment.Center) {
                    Text("✦", color = Color.White, fontSize = 17.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("AI 洞察", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = YS.brand, letterSpacing = 0.6.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        insightText(summary.categoryBreakdown),
                        fontSize = 11.5.sp, color = YS.inkSoft, lineHeight = 17.sp
                    )
                }
            }
        }

        // 最近记录
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("最近记录", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = YS.ink)
            Text("查看全部 ›", fontSize = 11.sp, color = YS.brand, fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeAll() })
        }

        if (recent.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("还没有记录，开启通知后自动入账", fontSize = 12.sp, color = YS.inkFaint)
            }
        } else {
            recent.forEach { tx -> RecentRow(tx) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(0.7f))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White,
            fontFamily = YS.fontNum, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun RecentRow(tx: Transaction) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(tx.category, 38)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.merchant, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = YS.ink, maxLines = 1)
            Text(
                "${tx.category} · ${formatDayHeader(tx.timestamp)}",
                fontSize = 10.sp, color = YS.inkFaint, maxLines = 1
            )
        }
        val isIncome = tx.type == TxType.INCOME
        Text(
            "${if (isIncome) "+" else "−"}${formatMoney(tx.amount)}",
            fontFamily = YS.fontNum,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) YS.income else YS.ink
        )
    }
}

private fun greetingText(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 11 -> "早上好"
        h < 14 -> "中午好"
        h < 18 -> "下午好"
        else -> "晚上好"
    }
}

private fun insightText(categories: List<Pair<String, Double>>): String {
    val top = categories.firstOrNull() ?: return "开始记账后，这里会给出你的消费洞察与建议。"
    val total = categories.sumOf { it.second }
    val pct = if (total > 0) (top.second / total * 100).toInt() else 0
    return "本月「${top.first}」支出占比最高，约 ${pct}%。留意这一项，能帮你更好地规划预算。"
}
