package otus.homework.customview.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateString(): String {
    val formatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    return formatter.format(Date(this)).also { println(it) }
}