package otus.homework.customview.utils

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.util.Locale

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.toDateString(): String = SimpleDateFormat("dd.MM.yyyy", LocaleRu).format(this * 1000L)
fun Long.toDateOnlyDayString(): String = SimpleDateFormat("dd", LocaleRu).format(this * 1000L)

val LocaleRu = Locale("ru", "RU")