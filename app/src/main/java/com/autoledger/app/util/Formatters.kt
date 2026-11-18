package com.autoledger.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFmt = SimpleDateFormat("M月d日", Locale.getDefault())
private val weekFmt = SimpleDateFormat("EEEE", Locale.getDefault())

fun formatTime(ts: Long): String = timeFmt.format(Date(ts))

fun formatDayHeader(ts: Long): String {
    val today = startOfDay(System.currentTimeMillis())
    val yesterday = startOfDay(System.currentTimeMillis() - 86_400_000L)
    val day = startOfDay(ts)
    return when (day) {
        today -> "今天 · ${dateFmt.format(Date(ts))}"
        yesterday -> "昨天 · ${dateFmt.format(Date(ts))}"
        else -> "${dateFmt.format(Date(ts))} ${weekFmt.format(Date(ts))}"
    }
}

fun formatMoney(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)

private fun startOfDay(ts: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
