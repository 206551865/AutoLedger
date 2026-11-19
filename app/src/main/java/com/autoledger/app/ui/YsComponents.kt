package com.autoledger.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.ui.theme.YS

/** 分类图标底色（有数配色） */
fun categoryTint(category: String): Color = when (category) {
    "餐饮" -> YS.expenseTint
    "交通" -> Color(0xFFE1EDF9)
    "购物" -> Color(0xFFF7E6EC)
    "生活缴费" -> YS.brandTint
    "娱乐" -> Color(0xFFEDE6F5)
    "医疗" -> Color(0xFFFBE6E1)
    "教育" -> Color(0xFFE8E5F5)
    "工资收入", "理财" -> YS.incomeTint
    "转账红包" -> Color(0xFFFBEFD9)
    else -> YS.surface2
}

/** 分类图标前景色 */
fun categoryInk(category: String): Color = when (category) {
    "餐饮" -> YS.expense
    "交通" -> YS.blue
    "购物" -> Color(0xFFC2557E)
    "生活缴费" -> YS.brand
    "娱乐" -> Color(0xFF8B6BB0)
    "医疗" -> Color(0xFFC15627)
    "教育" -> Color(0xFF6E63A8)
    "工资收入", "理财" -> YS.income
    "转账红包" -> Color(0xFFB8862B)
    else -> YS.inkSoft
}

fun categoryEmoji(category: String): String = when (category) {
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

/** 分类圆形图标 */
@Composable
fun CategoryIcon(category: String, size: Int = 40) {
    Box(
        Modifier.size(size.dp).background(categoryTint(category), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(categoryEmoji(category), fontSize = (size * 0.5).sp)
    }
}

/** 图例小圆点 */
@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = YS.inkSoft)
    }
}

/** 分类占比条颜色（分析页用） */
fun categoryBarColor(category: String): Color = when (category) {
    "餐饮" -> YS.brand
    "购物" -> YS.expense
    "交通" -> YS.blue
    "生活缴费" -> YS.income
    "娱乐" -> Color(0xFF8B6BB0)
    "医疗" -> Color(0xFFD9803A)
    "教育" -> YS.sand
    else -> YS.inkFaint
}
