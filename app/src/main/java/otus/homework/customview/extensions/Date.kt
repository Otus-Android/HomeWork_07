package otus.homework.customview.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.format(pattern: String = "dd.MM.yyyy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}