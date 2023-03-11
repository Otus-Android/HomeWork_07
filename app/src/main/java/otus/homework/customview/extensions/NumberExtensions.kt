package otus.homework.customview.extensions

import android.content.Context
import otus.homework.customview.R
import java.text.DecimalFormat

fun Float.displayPercentage(context: Context): String {
    val formatter = DecimalFormat("#.##")
    val text = formatter.format(this * 100).replace(",", ".")
    return context.getString(R.string.value_with_percent, text)
}