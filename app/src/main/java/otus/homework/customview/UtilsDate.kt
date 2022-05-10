package otus.homework.customview

import java.text.SimpleDateFormat
import java.util.*

fun Date.removeTime(): Date =
    Calendar.getInstance().apply {
        time = this@removeTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

fun Date.addDays(amount: Int): Date =
    Calendar.getInstance().apply {
        time = this@addDays
        add(Calendar.DATE, amount)
    }.time

fun Date.format(pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)