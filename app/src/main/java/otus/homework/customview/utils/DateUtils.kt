package otus.homework.customview.utils

import android.annotation.SuppressLint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("NewApi")
fun getLocalDateFromLong(time: Long): LocalDate {
    return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()
}

@SuppressLint("NewApi")
fun localDateToLong(date: LocalDate): Long {
    return date.toEpochDay()
}