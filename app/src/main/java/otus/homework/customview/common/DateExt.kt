package otus.homework.customview.common

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @return String represent of month name with three letter in name. Like "Jun"
 */
fun Date.getMonthName(): String {
    val dateCalendar = Calendar.getInstance().apply { time = this@getMonthName }
    val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())

    return monthFormatter.format(dateCalendar.time)
}

/**
 * @return String represent of day number with zero lead. Like "01" .. "15" .. "31"
 */
fun Date.getDayOfMonth(): String {
    val dateCalendar = Calendar.getInstance().apply { time = this@getDayOfMonth }
    val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())

    return dayFormatter.format(dateCalendar.time)
}

@SuppressLint("SimpleDateFormat")
fun Date.atDayStart(): Date {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    return dateFormatter.parse(dateFormatter.format(this)) ?: this
}