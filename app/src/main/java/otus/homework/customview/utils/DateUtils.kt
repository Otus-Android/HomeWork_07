package otus.homework.customview.utils

import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DateUtils @Inject constructor() {

    fun timestampToDayOfMonth(timestamp: Long): Int {
        val calendar = getCalendar(timestamp)
        return calendar[Calendar.DAY_OF_MONTH]
    }

    private fun getCalendar(payloadTimestamp: Long): Calendar {
        val date = Date(payloadTimestamp * MILLIS_IN_SECOND)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    companion object {
        private const val MILLIS_IN_SECOND = 1000
    }
}