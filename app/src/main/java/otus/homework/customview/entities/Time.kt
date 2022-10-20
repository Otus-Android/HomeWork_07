package otus.homework.customview.entities

import java.text.SimpleDateFormat
import java.util.*

class Time {

    fun timeToString(timeInSecond: Int): String {
        val dayInSecond = timeToDateInSeconds(timeInSecond)
        val timeInMillis = dayInSecond.toLong() * MILLIS_IN_SECOND
        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
        return sdf.format(timeInMillis)
    }

    fun timeToDayAndMonthString(timeInSecond: Int): String {
        val dayInSecond = timeToDateInSeconds(timeInSecond)
        val timeInMillis = dayInSecond.toLong() * MILLIS_IN_SECOND
        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("dd-MM", locale)
        return sdf.format(timeInMillis)
    }

    fun timeToDateInSeconds(time: Int) = time / SECONDS_IN_DAY * SECONDS_IN_DAY

    companion object {
        private const val MILLIS_IN_SECOND = 1000
        private const val SECONDS_IN_DAY = 24 * 60 * 60

    }
}