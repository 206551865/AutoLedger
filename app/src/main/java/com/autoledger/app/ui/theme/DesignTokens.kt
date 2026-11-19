package com.autoledger.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.autoledger.app.R

/**
 * 「有数 Youshu」设计系统 Token。
 * 换算自设计稿 oklch 色彩，遵循暖象牙底 + 松绿品牌 + 精致编辑感。
 */
object YS {
    // Surfaces — warm ivory, never pure white
    val bg = Color(0xFFF6F3EC)
    val surface = Color(0xFFFDFCF9)
    val surface2 = Color(0xFFF0ECE4)
    val surface3 = Color(0xFFE6E1D6)

    // Ink — deep pine-tinted neutrals
    val ink = Color(0xFF1B2720)
    val inkSoft = Color(0xFF535E58)
    val inkFaint = Color(0xFF8B958F)

    // Brand — pine green
    val brand = Color(0xFF156B4A)
    val brandDeep = Color(0xFF004E31)
    val brandTint = Color(0xFFDEF5E8)
    val brandGlow = Color(0xFFBEEAD3)

    // Semantic — expense = terracotta, income = green
    val expense = Color(0xFFC15627)
    val expenseTint = Color(0xFFFFE9E0)
    val income = Color(0xFF2B7A4D)
    val incomeTint = Color(0xFFE0F5E6)

    // Chart accents
    val blue = Color(0xFF6DA9E7)
    val sand = Color(0xFFD9AF75)

    val line = Color(0xFFE2DED5)
    val white = Color.White

    // Radius
    val rHero = RoundedCornerShape(24.dp)
    val rCard = RoundedCornerShape(20.dp)
    val rInput = RoundedCornerShape(18.dp)
    val rIcon = RoundedCornerShape(12.dp)
    val rPill = RoundedCornerShape(100)

    // Typography — 数字用 Fraunces 衬线，UI 用 Jakarta，中文走系统
    val fontNum = FontFamily(Font(R.font.fraunces))
    val fontUI = FontFamily(Font(R.font.jakarta))
}

/** 兼容旧代码里的颜色引用 */
object LedgerColors {
    val Expense = YS.expense
    val Income = YS.income
}
