package otus.homework.customview

import android.content.Context
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private val amountFormat: DecimalFormat by lazy(LazyThreadSafetyMode.NONE) {
    val formatSymbols = DecimalFormatSymbols()
    formatSymbols.groupingSeparator = ' '
    return@lazy DecimalFormat("#,##0 ₽", formatSymbols)
}
private val format = DecimalFormat("0").apply { roundingMode = RoundingMode.UP }
private var dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

fun Context.asPixel(dp: Int): Int = (dp * resources.displayMetrics.density).roundToInt()

fun Int.toMoneyFormat(): String =
    amountFormat.format(this)

fun Double.roundFormat(): String = format.format(this)

fun Date.formatToString(): String = dateFormat.format(this)
