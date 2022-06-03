package otus.homework.customview

import android.content.res.Resources

fun Float.dPToPx() = (this * Resources.getSystem().displayMetrics.density)

fun Float.pxToDp() = (this / Resources.getSystem().displayMetrics.density)

fun Int.dPToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.pxToDp() = (this / Resources.getSystem().displayMetrics.density).toInt()