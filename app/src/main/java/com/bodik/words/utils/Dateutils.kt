package com.bodik.words.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatReminderDate(timeMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

    val todayYear = now.get(Calendar.YEAR)
    val todayDay = now.get(Calendar.DAY_OF_YEAR)
    val targetYear = target.get(Calendar.YEAR)
    val targetDay = target.get(Calendar.DAY_OF_YEAR)

    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMillis))

    return when {
        targetYear == todayYear && targetDay == todayDay - 1 -> "Вчера, $time"
        targetYear == todayYear && targetDay == todayDay -> "Сегодня, $time"
        targetYear == todayYear && targetDay == todayDay + 1 -> "Завтра, $time"
        else -> SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(timeMillis))
    }
}
