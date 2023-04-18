package otus.homework.customview

import android.content.res.Resources
import android.util.DisplayMetrics

fun Int.dpToPx(res: Resources): Int =
    (this.toFloat() * (res.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
