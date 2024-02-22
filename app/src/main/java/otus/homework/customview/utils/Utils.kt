package otus.homework.customview.utils

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import java.util.Calendar
import java.util.Date

fun View.convertToDp(number: Int) = (resources.displayMetrics.density * number).toInt()

fun View.spToPx(sp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

fun convertUnixTimestampToDate(unixTimestamp: Long) = Date(1000 * unixTimestamp)

fun generateRandomColor(): Int {
    val red = (Math.random() * 256).toInt()
    val green = (Math.random() * 256).toInt()
    val blue = (Math.random() * 256).toInt()

    return Color.rgb(red, green, blue)
}

fun areDatesOnSameDay(o1: Date, o2: Date): Boolean {
    val cal1: Calendar = Calendar.getInstance().apply {
        time = o1
    }
    val cal2 = Calendar.getInstance().apply {
        time = o2
    }

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

fun getNextDay(day: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = day
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    return Date(calendar.timeInMillis)
}