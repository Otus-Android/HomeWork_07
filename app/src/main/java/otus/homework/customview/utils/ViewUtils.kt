package otus.homework.customview.utils

import android.content.res.Resources


fun Float.dpToPx(resources: Resources): Int {
    return (this * resources.displayMetrics.density).toInt()
}