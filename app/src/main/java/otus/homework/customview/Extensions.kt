package otus.homework.customview

import android.content.res.Resources
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)

fun Long.asDate(): String {
    val sdf = SimpleDateFormat("dd.MM", Locale.ROOT)
    val netDate = Date(this * 1000)

    return sdf.format(netDate).replace(' ', '\n')
}