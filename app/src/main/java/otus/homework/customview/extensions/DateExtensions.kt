package otus.homework.customview.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.getFormattedDate(
    pattern: String = "dd/MM/yyyy"
): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

fun Date.convertToDayMillis(): Long {
    val initialCalendar = Calendar.getInstance().apply {
        time = this@convertToDayMillis
    }

    val formattedCalendar = Calendar.getInstance().apply {
        set(
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
        set(Calendar.MILLISECOND, 0)
    }

    return formattedCalendar.timeInMillis
}