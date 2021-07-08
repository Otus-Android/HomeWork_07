package otus.homework.customview.ext

import android.content.res.Resources

fun Int.dPToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()