package otus.homework.customview.utils

import java.util.Calendar

fun Long.toDateWithoutTime(): Long = Calendar.getInstance()
    .also { calendar ->
        calendar.timeInMillis = this
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }.timeInMillis

fun Long.addDays(numberOfDays: Int): Long = Calendar.getInstance()
    .also { calendar ->
        calendar.timeInMillis = this
        calendar.add(Calendar.DAY_OF_YEAR, numberOfDays)
    }.timeInMillis